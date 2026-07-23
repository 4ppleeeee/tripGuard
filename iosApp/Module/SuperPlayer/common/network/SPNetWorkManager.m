/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNetWorkManager.m
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 16/3/28
 Description :
 History     : 16/3/28 初始版本
 ***********************************************************/

#import "SPNetWorkManager.h"
//#import "SPNACManager.h"
#import "SPATSHTTPRequest.h"

@interface SPNetWorkManager ()

@property (nonatomic, strong) NSMutableSet *requestSet;  //防止请求对象被释放，导致野指针

@end

@implementation SPNetWorkManager

+ (instancetype)shareInstance {
    static SPNetWorkManager *instance = nil;
    static dispatch_once_t onceToken;

    dispatch_once(&onceToken, ^{
        instance = [[SPNetWorkManager alloc] init];
        instance.requestSet = [[NSMutableSet alloc] initWithCapacity:32];
    });
    return instance;
}

//发送一个get请求
- (nullable id)getRequest:(nonnull NSString *)url
           requestHeaders:(nullable NSDictionary *)requestHeaders
        completionHandler:(nullable GetPostRequestCompletionHandlerBlock)completionHandler {
    SPATSHTTPRequest *request = [[SPATSHTTPRequest alloc] init];
    @synchronized(self) {
        [self.requestSet addObject:request];
    }

    __weak typeof(self) weakSelf = self;
    __weak typeof(request) weakRequest = request;
    NSURL *httpURL = [NSURL URLWithString:url];
    [request get:httpURL
           requestHeaders:requestHeaders
          timeoutInterval:[self getNetworkTimeOutInterval]
        completionHandler:^(NSData *_Nullable data, NSHTTPURLResponse *_Nullable response, NSError *_Nullable error) {
            __strong typeof(weakSelf) stongSelf = weakSelf;
            if (completionHandler != nil) {
                [self postGetRequestCompletionWtihData:data httpResponse:response error:error completionHandler:completionHandler];
            }
            //执行结束，将request对象移除,这里 self 不使用weak，是因为这里的self本身就是一个单例
            @synchronized(stongSelf) {
                [stongSelf.requestSet removeObject:weakRequest];
            }
        }];

    return request;
}

//发送一个post 请求
- (nullable id)postRequest:(nonnull NSString *)url
            requestHeaders:(nullable NSDictionary *)requestHeaders
                  postData:(nullable NSData *)postData
         completionHandler:(nullable GetPostRequestCompletionHandlerBlock)completionHandler {
    SPATSHTTPRequest *request = [[SPATSHTTPRequest alloc] init];
    @synchronized(self) {
        [self.requestSet addObject:request];
    }

    __weak typeof(self) weakSelf = self;
    __weak typeof(request) weakRequest = request;
    NSURL *httpURL = [NSURL URLWithString:url];
    [request post:httpURL
           requestHeaders:requestHeaders
                 postData:postData
          timeoutInterval:[self getNetworkTimeOutInterval]
        completionHandler:^(NSData *_Nullable data, NSHTTPURLResponse *_Nullable response, NSError *_Nullable error) {
            __strong typeof(weakSelf) stongSelf = weakSelf;
            if (completionHandler != nil) {
                [self postGetRequestCompletionWtihData:data httpResponse:response error:error completionHandler:completionHandler];
            }
            //执行结束，将request对象移除,这里 self 不使用weak，是因为这里的self本身就是一个单例,另外，这里 self 并不直接持有这个block
            @synchronized(stongSelf) {
                [stongSelf.requestSet removeObject:weakRequest];
            }
        }];
    return request;
}

- (void)cancelRequestWithTask:(nonnull id)task;
{
    if ([task isKindOfClass:[SPATSHTTPRequest class]] == NO) {
        return;
    }
    SPATSHTTPRequest *request = (SPATSHTTPRequest *)task;
    if (request != nil) {
        @synchronized(self) {
            [request cancel];
            [self.requestSet removeObject:request];  // cancel后也需要直接移除
        }
    }
}

- (void)postGetRequestCompletionWtihData:(nullable NSData *)data
                            httpResponse:(nullable NSHTTPURLResponse *)response
                                   error:(nullable NSError *)error
                       completionHandler:(nonnull GetPostRequestCompletionHandlerBlock)completionHandler {
    if (data != nil && response != nil && error == nil && response.statusCode == 200) {
        // 没有error并且状态码为200
        completionHandler(data, nil);
    } else {
        if (error != nil) {
            // error不为空，说明网络层有错误
            NSError *errInfo = [NSError errorWithDomain:@"SPNetWorkManager Post Get"
                                                   code:(error.code + gSPNetWorkResultCode_HTTP_Response_End)
                                               userInfo:error.userInfo];
            completionHandler(nil, errInfo);
        } else {
            NSError *errInfo = nil;
            if (response != nil) {
                errInfo = [NSError errorWithDomain:@"SPNetWorkManager Post Get" code:response.statusCode userInfo:nil];
            } else {
                errInfo = [NSError errorWithDomain:@"SPNetWorkManager Post Get" code:gSPNetWorkResultCode_PostGetErr userInfo:nil];
            }
            completionHandler(nil, errInfo);
        }
    }
}

//得到当前的超时时间
- (NSTimeInterval)getNetworkTimeOutInterval {
    return SPSDKCONF_NETWORK_TIME_OUT_INTERVAL;
}

@end
