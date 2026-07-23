/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPHTTPRequest.m
 Author      : jarenzhang
 Version     : 1.0
 Date        : 2016/11/28
 Description :
 History     : 2016/11/28 初始版本
 ***********************************************************/

#import "SPHTTPRequest.h"

@implementation SPHTTPRequest

- (void)getRequest:(nonnull NSURL *)url requestHeaders:(nullable NSDictionary *)requestHeaders timeoutInterval:(NSTimeInterval)timeoutInterval {
    // NOLINTNEXTLINE
    @throw [[NSException alloc] initWithName:@"SPHTTPRequest Exception" reason:@"请使用子类的实现" userInfo:nil];
}

- (void)postRequest:(nonnull NSURL *)url
     requestHeaders:(nullable NSDictionary *)requestHeaders
           postData:(nullable NSData *)postData
    timeoutInterval:(NSTimeInterval)timeoutInterval {
    // NOLINTNEXTLINE
    @throw [[NSException alloc] initWithName:@"SPHTTPRequest Exception" reason:@"请使用子类的实现" userInfo:nil];
}

- (void)cancelRequest {
    // NOLINTNEXTLINE
    @throw [[NSException alloc] initWithName:@"SPHTTPRequest Exception" reason:@"请使用子类的实现" userInfo:nil];
}

//默认实现直接返回
- (nullable NSData *)responseDataWithRawResponseData:(nullable NSData *)rawData {
    return rawData;
}

- (void)finishTasksAndInvalidate {
}
@end
