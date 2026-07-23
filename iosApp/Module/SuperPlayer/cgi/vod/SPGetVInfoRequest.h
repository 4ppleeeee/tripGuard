//
//  SPVideoInfoRequest.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/27.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIRequestParam.h"
#import "SPMediaPlayInfo.h"
#import "SPGetVInfoData.h"
#import "SPCGIBase.h"

@class SPGetVInfoRequest;

@protocol SPGetVInfoRequestDelegate <NSObject>

@optional

- (void)request:(SPGetVInfoRequest *)request onGetVInfoData:(SPGetVInfoData *)getvinfoData requestID:(int)requestID;

- (void)request:(SPGetVInfoRequest *)request onGetVInfoFailed:(NSError *)error requestID:(int)requestID;

@end

@interface SPGetVInfoRequest : SPCGIBase

@property (nonatomic, weak) id<SPGetVInfoRequestDelegate> delegate;

- (instancetype)initWithParam:(SPCGIInitParam *)param;

- (int)requestWithParam:(SPCGIRequestParam *)requestParam;

- (void)stopWithRequestID:(int)requestID;

@end
