/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPATSHTTPRequest.m
 Author      : jarenzhang
 Version     : 1.0
 Date        : 2016/11/29
 Description :
 History     : 2016/11/29 初始版本
 ***********************************************************/

#import "SPATSHTTPRequest.h"
#import "SPNSURLSessionHTTPRequest.h"
#import "SPVcSystemInfo.h"

@interface SPATSHTTPRequest ()

@property (atomic, strong) SPHTTPRequest *httpRequest;
@property (nonatomic, copy) SPATSHTTPRequestCompletionHandler callbackBlock;
@property (nonatomic, strong) NSMutableData *responseData;

@end

@implementation SPATSHTTPRequest

- (void)get:(nonnull NSURL *)url
       requestHeaders:(nullable NSDictionary *)requestHeaders
      timeoutInterval:(NSTimeInterval)timeoutInterval
    completionHandler:(nullable SPATSHTTPRequestCompletionHandler)completionHandler {
    if (self.httpRequest != nil) {
        return;
    }
    self.httpRequest = [self createHTTPRequest:url];
    self.httpRequest.delegate = self;
    self.callbackBlock = completionHandler;
    [self.httpRequest getRequest:url requestHeaders:requestHeaders timeoutInterval:timeoutInterval];
}

- (void)post:(nonnull NSURL *)url
       requestHeaders:(nullable NSDictionary *)requestHeaders
             postData:(nullable NSData *)postData
      timeoutInterval:(NSTimeInterval)timeoutInterval
    completionHandler:(nullable SPATSHTTPRequestCompletionHandler)completionHandler {
    if (self.httpRequest != nil) {
        return;
    }
    self.httpRequest = [self createHTTPRequest:url];
    self.httpRequest.delegate = self;
    self.callbackBlock = completionHandler;
    [self.httpRequest postRequest:url requestHeaders:requestHeaders postData:postData timeoutInterval:timeoutInterval];
}

- (void)cancel {
    self.callbackBlock = nil;
    self.responseData = nil;
    if (self.httpRequest != nil) {
        [self.httpRequest cancelRequest];
        self.httpRequest = nil;
    }
}

#pragma mark -

- (void)request:(SPHTTPRequest *)request didReceiveData:(NSData *)data {
    if (self.httpRequest == request) {
        [self.responseData appendData:data];
    }
}

- (void)requestFinished:(SPHTTPRequest *)request error:(NSError *)error {
    if (self.httpRequest != request) {
        return;
    }

    //这里注意，对于ASI库的回包结果，需要在拼接完毕后，根据是否是Gzip来决定要不要解开一下
    NSData *responseData = [self.httpRequest responseDataWithRawResponseData:self.responseData];
    if (self.callbackBlock != nil) {
        self.callbackBlock(responseData, self.httpRequest.httpURLResponse, error);
    }

    //请求完成，将相关的数据重置
    @synchronized(self) {
        [self.httpRequest finishTasksAndInvalidate];
        self.httpRequest.delegate = nil;
        self.httpRequest = nil;
    }
    //将callback及时释放
    self.callbackBlock = nil;
}

#pragma mark -

//  4. CGI请求（部分是HTTPS，部分不是） 【这个逻辑会由 SPNetWorkManager 封装完成，业务不用关心】
//      * 如果是 HTTPS，不用管版本信息，直接使用NSURLSession 搞定即可
//      * 如果是 HTTP
//          a, iOS8 及其之前的版本，直接使用 NSURLSession 就可以了 URL不用做任何的处理，不受IPV6和ATS的影响
//          b, iOS9 需要使用 SPASIHTTPRequest （不需要对URL做任何IPV6的适配）
//          c, iOS10 及其以上
//              1,URL 里面如果是 IP，则使用 NSURLSession来处理请求（不需要对URL做任何IPV6的适配）。
//              2,如果 URL里面是域名，则使用 SPASIHTTPRequest 来处理请求（不需要对URL做任何IPV6的适配）

//一个工厂方法来创建请求对象
- (SPHTTPRequest *)createHTTPRequest:(nonnull NSURL *)url {
    self.responseData = [[NSMutableData alloc] init];
    //先强制走URLSession，后续解决asi cancel问题再按照下面的策略 charli

    //    return [[SPNSURLSessionHTTPRequest alloc] init];

    //等之后有ATS需求在解除注射
    if ([[SPVcSystemInfo sharedInstance] isAllowsArbitraryLoads]) {
        return [[SPNSURLSessionHTTPRequest alloc] init];
    }

    if ([@"https" caseInsensitiveCompare:url.scheme] == NSOrderedSame) {
        return [[SPNSURLSessionHTTPRequest alloc] init];
    }
    return [[SPNSURLSessionHTTPRequest alloc] init];
}

@end
