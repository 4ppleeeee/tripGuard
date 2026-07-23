/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPHTTPRequest.h
 Author      :
 Version     : 1.0
 Date        : // SPHTTPReques，基类，不允许直接使用，必须使用它的派生类
 Description :
 History     : // SPHTTPReques，基类，不允许直接使用，必须使用它的派生类 初始版本
 ***********************************************************/
//  Created by jarenzhang on 2016/11/28.
//  Copyright © 2016年 Tencent Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPHTTPRequestDelegate.h"

@interface SPHTTPRequest : NSObject

@property (atomic, weak, nullable) id<SPHTTPRequestDelegate> delegate;
@property (nonatomic, strong, nullable) NSHTTPURLResponse *httpURLResponse;  //请求的到回包以后，相关的http头信息

- (void)getRequest:(nonnull NSURL *)url
    requestHeaders:(nullable NSDictionary<NSString *, NSString *> *)requestHeaders
   timeoutInterval:(NSTimeInterval)timeoutInterval;// NOLINT

- (void)postRequest:(nonnull NSURL *)url
     requestHeaders:(nullable NSDictionary<NSString *, NSString *> *)requestHeaders
           postData:(nullable NSData *)postData
    timeoutInterval:(NSTimeInterval)timeoutInterval;

- (void)cancelRequest;

- (nullable NSData *)responseDataWithRawResponseData:(nullable NSData *)rawData;

- (void)finishTasksAndInvalidate;

@end
