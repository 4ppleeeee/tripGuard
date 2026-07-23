//
//  SPVODRequestParam.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/17.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIRequestParam.h"
#import "SPCGICapabilityParam.h"

@interface SPVODRequestParam : SPCGIRequestParam

@property (nonatomic, assign) SPGetVInfoRequestType getvinfoReqType;  // getvinfo请求类型

@property (nonatomic, assign) NSTimeInterval startPosition;  // 跳过片头，单位为秒，默认为0

@property (nonatomic, assign) NSTimeInterval skipEndPosition;  // 跳过片尾，单位为秒，默认为0

@property (nonatomic, copy) NSString *track;  // 请求的独立音轨名称

@property (nonatomic, assign) int currentPlayPosition;  // 当前播放的时间点

@property (nonatomic, copy) NSString *previd;  // 如果传入previd，则为秒播

@property (nonatomic, strong) SPVODCapabilityParam *capabilityParam;  // 能力参数

@end
