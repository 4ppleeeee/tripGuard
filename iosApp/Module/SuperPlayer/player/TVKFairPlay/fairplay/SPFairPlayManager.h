/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPFairPlayManager.h
 Author      : fusionxu(许福生)
 Version     : 1.0
 Date        : 14/01/2018
 Description :
 History     : 14/01/2018 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import "SPMediaPlayInfo.h"
#import "SPVODPlayInfo.h"

@interface SPFairPlayManager : NSObject

- (instancetype)initWithMediaPlayInfo:(SPVODPlayInfo *)mediaPlayInfo;

- (BOOL)startRequestCKCWithRequest:(AVAssetResourceLoadingRequest *)loadingRequest;

- (void)cancelRequset;

@end
