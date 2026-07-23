//
//  SPLiveInfoRequest.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/5.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIRequestParam.h"
#import "SPMediaPlayInfo.h"
#import "SPLivePlayInfo.h"
#import "SPCGIBase.h"

@class SPLiveInfoRequest;

@protocol SPLiveInfoRequestDelegate <NSObject>

@optional

- (void)request:(SPLiveInfoRequest *)request onGetLiveInfo:(SPLivePlayInfo *)playInfo requestID:(int)requestID;

- (void)request:(SPLiveInfoRequest *)request onGetLiveInfoFailed:(NSError *)error requestID:(int)requestID;

@end

@interface SPLiveInfoRequest : SPCGIBase

@property (nonatomic, weak) id<SPLiveInfoRequestDelegate> delegate;

- (instancetype)initWithParam:(SPCGIInitParam *)param;

- (int)requestWithParam:(SPCGIRequestParam *)requestParam;

- (void)stopWithRequestID:(int)requestID;

@end
