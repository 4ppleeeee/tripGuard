/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNSURLSessionHTTPRequest.m
 Author      : jarenzhang
 Version     : 1.0
 Date        : 2016/11/28
 Description :
 History     : 2016/11/28 初始版本
 ***********************************************************/

#import "SPNSURLSessionHTTPRequest.h"

@interface SPNSURLSessionHTTPRequest ()

@property (atomic, weak) NSURLSessionDataTask *task;
@property (readwrite, nonatomic, strong) NSURLSession *session;

@end

@implementation SPNSURLSessionHTTPRequest

- (instancetype)init {
    self = [super init];
    if (self) {
        self.session = [NSURLSession sessionWithConfiguration:[NSURLSessionConfiguration defaultSessionConfiguration]
                                                     delegate:self
                                                delegateQueue:nil];
    }
    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"<%@: %p, session: %@>", NSStringFromClass([self class]), self, self.session];
}

- (void)dealloc {
}

- (void)getRequest:(nonnull NSURL *)url
    requestHeaders:(nullable NSDictionary<NSString *, NSString *> *)requestHeaders
   timeoutInterval:(NSTimeInterval)timeoutInterval {// NOLINT
    //如果有请求没有完成，直接返回
    if (self.task != nil) {
        return;
    }

    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url
                                                                cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                                                            timeoutInterval:timeoutInterval];
    [request setHTTPMethod:@"GET"];
    [request setAllHTTPHeaderFields:requestHeaders];

    NSURLSessionDataTask *task = [self.session dataTaskWithRequest:request];
    [task resume];
    //哪个线程调用过来，就从哪个线程调用回去
    if ([self.delegate respondsToSelector:@selector(requestStarted:)]) {
        [self.delegate requestStarted:self];
    }
}

- (void)postRequest:(nonnull NSURL *)url
     requestHeaders:(nullable NSDictionary<NSString *, NSString *> *)requestHeaders
           postData:(nullable NSData *)postData
    timeoutInterval:(NSTimeInterval)timeoutInterval {
    //如果有请求没有完成，直接返回
    if (self.task != nil) {
        return;
    }

    NSMutableURLRequest *request =
        [[NSMutableURLRequest alloc] initWithURL:url
                                     cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                                 timeoutInterval:timeoutInterval];
    [request setHTTPMethod:@"POST"];
    [request setAllHTTPHeaderFields:requestHeaders];
    [request setHTTPBody:postData];

    NSURLSessionDataTask *task = [self.session dataTaskWithRequest:request];
    [task resume];
    //哪个线程调用过来，就从哪个线程调用回去
    if ([self.delegate respondsToSelector:@selector(requestStarted:)]) {
        [self.delegate requestStarted:self];
    }
}

- (void)cancelRequest {
    self.delegate = nil;
    [self.task cancel];
    [self.session invalidateAndCancel];
}

- (void)finishTasksAndInvalidate {
    [self.session finishTasksAndInvalidate];
}

#pragma mark -

- (void)URLSession:(NSURLSession *)session
          dataTask:(NSURLSessionDataTask *)dataTask
didReceiveResponse:(NSURLResponse *)response
 completionHandler:(void (^)(NSURLSessionResponseDisposition disposition))completionHandler {// NOLINT
    NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
    self.httpURLResponse = httpResponse;
    if ([self.delegate respondsToSelector:@selector(request:didReceiveResponseHeaders:)]) {
        [self.delegate request:self didReceiveResponseHeaders:httpResponse.allHeaderFields];
    }

    completionHandler(NSURLSessionResponseAllow);
}

- (void)URLSession:(NSURLSession *)session dataTask:(NSURLSessionDataTask *)dataTask didReceiveData:(NSData *)data {
    if ([self.delegate respondsToSelector:@selector(request:didReceiveData:)]) {
        [self.delegate request:self didReceiveData:data];
    }
}

- (void)URLSession:(NSURLSession *)session task:(NSURLSessionTask *)task didCompleteWithError:(nullable NSError *)error {
    if ([self.delegate respondsToSelector:@selector(requestFinished:error:)]) {
        [self.delegate requestFinished:self error:error];
    }

    [session finishTasksAndInvalidate];
}

@end
