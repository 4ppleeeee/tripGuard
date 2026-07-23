/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPResourceLoader.h
 Author      : GHL
 Version     : 1.0
 Date        : 16/9/7
 Description :
 History     : 16/9/7 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import "SPVODPlayInfo.h"

@protocol SPResourceLoaderDelegate <NSObject>

@optional
- (void)onFairplayRequestError;

@end

@interface SPResourceLoader : NSObject <AVAssetResourceLoaderDelegate>

@property (nonatomic, weak) id<SPResourceLoaderDelegate> delegate;

@property (nonatomic, strong) SPVODPlayInfo *mediaPlayInfo;

- (void)cleanResource;

@end
