//
//  SPLiveRequestParam.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/17.
//  Copyright © 2019 tencent. All rights reserved.
//

#import "SPCGIRequestParam.h"
#import "SPCGICapabilityParam.h"

/** 直播请求类型 */
typedef NS_ENUM(NSUInteger, SPLiveRequestType) {
    SPLiveRequestTypePlay = 0,     //获取播放地址和vkey
    SPLiveRequestTypePreview = 1,  //仅查询信息,如试看次数等
};

@interface SPLiveRequestParam : SPCGIRequestParam

@property (nonatomic, assign) SPLiveRequestType requestType;  // 请求类型

@property (nonatomic, assign) int64_t userLiveSeeBackTime;  // 直播回看时间点，该值大于0则认为是回看，如果不是回看，请传0

@property (nonatomic, copy) NSString *p2pVersion;

@property (nonatomic, strong) SPLiveCapabilityParam *capabilityParam;  // 能力参数

@end
