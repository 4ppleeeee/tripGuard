/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPNSURLSessionHTTPRequest.h
 Author      :
 Version     : 1.0
 Date        : // 将系统的URLSession进行包裹，方便ATS情况下进行相关的切换
 Description :
 History     : // 将系统的URLSession进行包裹，方便ATS情况下进行相关的切换 初始版本
 ***********************************************************/
//  Copyright © 2016年 Tencent Inc. All rights reserved.
//

#import "SPHTTPRequest.h"

@interface SPNSURLSessionHTTPRequest : SPHTTPRequest <NSURLSessionDataDelegate>

@end
