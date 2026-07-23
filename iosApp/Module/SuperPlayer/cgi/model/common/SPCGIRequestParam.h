/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPCGIRequestParam.h
 * @brief    CGI请求参数
 * @author   hemanli
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"
#import "SPCGIDefines.h"
#import "SPCGIRequestCommonParam.h"
#import "SPCGIRequestOptions.h"

/**
 * cgi请求所需要携带的参数
 */
@interface SPCGIRequestParam : NSObject

@property (nonatomic, assign) int playSeq;  // 一次播放的sequence

@property (nonatomic, copy) NSString *flowID;  //  全局唯一标识一次播放，一次播放过程中不变，包括切清晰度

@property (nonatomic, copy) NSString *vid;  // 必填字段，如果是点播，则为video ID, 如果是直播，则为流ID（Stream ID），

@property (nonatomic, copy) NSString *cid;  // 专辑id，如果是点播，则为专辑ID（Cover ID），
                                            // 如果是直播，则为PID，如果播的是专辑中某个剧集，该字段为必要字断

@property (nonatomic, copy) NSString *srccontenid;  // 源内容ID，短带长的短视频ID

@property (nonatomic, copy) NSString *definition;  // 非必要，若不填，则默认为"auto"，请见SPPlayerDefine.h

@property (nonatomic, assign) SPMediaFormat mediaFormat;  // 非必要字断，要请求的媒体类型，没有特殊要求，填SPMediaFormatAuto即可

@property (nonatomic, assign) BOOL needCharge;  // 该视频是否为收费视频，默认为NO

@property (nonatomic, copy) NSDictionary<NSString *, NSString *> *freeFlowParam;  // 运营商免流码，由SDK外部透传，SDK不理解

@property (nonatomic, assign) BOOL isDLNA;  // 是否dlna，默认为NO

@property (nonatomic, assign) BOOL isAirplay;  // 是否dlna，默认为NO

@property (nonatomic, strong) SPCGIRequestCommonParam *commonParams;

@property (nonatomic, strong) NSDictionary<NSString *, NSString *> *extraParams;  // SDK外部传进来的扩展字段

@property (nonatomic, strong) SPCGIRequestOptions *options;
@end
