//
//  SPVBKeyRequest.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPVODRequestParam.h"
#import "SPVODPlayInfo.h"
#import "SPGetVInfoData.h"
#import "SPGetVBKeyData.h"
#import "SPCGIBase.h"

@interface SPVBKeyRequestParam : NSObject

@property (nonatomic, strong) SPVODRequestParam *vodReqParam;

@property (nonatomic, strong) SPGetVInfoData *getvinfoData;

@property (nonatomic, assign) int beginIndex;

@property (nonatomic, assign) int endIndex;

@end

@class SPVBKeyRequest;
@protocol SPVBKeyRequestDelegate <NSObject>

@optional
- (void)request:(SPVBKeyRequest *)request onGeSPeyData:(SPGetVBKeyData *)geSPeyData requestID:(int)requestID;

- (void)request:(SPVBKeyRequest *)request onGeSPeyFailed:(NSError *)error requestID:(int)requestID;

@end

@interface SPVBKeyRequest : SPCGIBase

@property (nonatomic, weak) id<SPVBKeyRequestDelegate> delegate;

- (instancetype)initWithParam:(SPCGIInitParam *)param;

- (int)requestWithParam:(SPVBKeyRequestParam *)requestParam;

- (void)stop;

@end
