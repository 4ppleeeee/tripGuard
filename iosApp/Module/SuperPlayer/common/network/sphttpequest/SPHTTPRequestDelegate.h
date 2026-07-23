/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPHTTPRequestDelegate.h
 Author      : jarenzhang
 Version     : 1.0
 Date        : 2016/11/28
 Description :
 History     : 2016/11/28 初始版本
 ***********************************************************/

#pragma once

@class SPHTTPRequest;

@protocol SPHTTPRequestDelegate <NSObject>

@optional

- (void)requestStarted:(SPHTTPRequest *)request;

- (void)request:(SPHTTPRequest *)request didReceiveResponseHeaders:(NSDictionary *)responseHeaders;

- (void)request:(SPHTTPRequest *)request didReceiveData:(NSData *)data;

- (void)requestFinished:(SPHTTPRequest *)request error:(NSError *)error;

@end
