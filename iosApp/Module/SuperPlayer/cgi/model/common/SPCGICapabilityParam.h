//
//  SPCGICapabilityParam.h
//  SPPlayer
//
//  Created by liyukuan on 2019/9/25.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIDefines.h"

@interface SPCGICapabilityParam : NSObject

@property (nonatomic, assign) SPHEVCLevel hevcLevel;  // HEVC能力值，请见SPHEVCLevel

@property (nonatomic, assign) int spvideo;  // 视频能力，bitset形式，请见SPVideoCapability的定义

@property (nonatomic, assign) int spaudio;  // 音频能力，bitset形式，请见SPAudioCapability的定义

@property (nonatomic, assign) int drm;  // drm能力，bitset形式，请见TVKDRMCapability的定义

@end

@interface SPVODCapabilityParam : SPCGICapabilityParam

@property (nonatomic, assign) TVKWaterMarkCapability spwm;  // 软水印请见TVKWaterMarkCapability的定义

@property (nonatomic, assign) SPSRTCapability spsrt;

@property (nonatomic, assign) int defnPayVer;  // 清晰度付费开关，bitset形式，见SPDefnPayVer的定义

@property (nonatomic, copy) NSString *spptype;  // 付费类型状态, 字符串，见SPCGIDefines.h的定义

@property (nonatomic, assign) int sphls;  // sphls=2表示m3u8直出

@property (nonatomic, assign) int spgzip;  // spgzip=1支持返回数据采用gzip压缩

@end

@interface SPLiveCapabilityParam : SPCGICapabilityParam

@property (nonatomic, assign) int active_sp;  // 控制spvideo和spaudio是否生效，0:不生效，1:不生效, TODO hemanli

@property (nonatomic, assign) BOOL enableLiveQueue;  // 直播排队

@end
