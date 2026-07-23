/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPATSHTTPRequest.h
 Author      : jarenzhang
 Version     : 1.0
 Date        : 2016/11/29
 Description :
 History     : 2016/11/29 初始版本
 ***********************************************************/
//  这里处理 ASI 和 URLSession 在不同版本的兼容，提供一个直接满足ATS的封装
//

#import <Foundation/Foundation.h>
#import "SPHTTPRequestDelegate.h"

typedef void (^SPATSHTTPRequestCompletionHandler)(NSData *_Nullable data, NSHTTPURLResponse *_Nullable response, NSError *_Nullable error);

@interface SPATSHTTPRequest : NSObject <SPHTTPRequestDelegate>

- (void)get:(nonnull NSURL *)url
       requestHeaders:(nullable NSDictionary *)requestHeaders
      timeoutInterval:(NSTimeInterval)timeoutInterval
    completionHandler:(nullable SPATSHTTPRequestCompletionHandler)completionHandler;

- (void)post:(nonnull NSURL *)url
       requestHeaders:(nullable NSDictionary *)requestHeaders
             postData:(nullable NSData *)postData
      timeoutInterval:(NSTimeInterval)timeoutInterval
    completionHandler:(nullable SPATSHTTPRequestCompletionHandler)completionHandler;

- (void)cancel;

@end
