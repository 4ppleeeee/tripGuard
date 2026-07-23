/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPMediaPlayInfo.h
 * @brief    播放所需信息，主要是由CGI返回的信息
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>
#import "SPMediaInfo.h"
#import "SPCGIDefines.h"
#import "SPDefinitionModel.h"
#import "SPSection.h"

@interface SPMediaPlayInfo : NSObject

@property (nonatomic, assign) int playID;                                   // 播放序号，用于标识一次播放，由cgi模块填充

@property (atomic, copy) NSString *vid;                                     // 点播时为video ID，直播时为流ID

@property (atomic, copy) NSString *coverID;                                 // 专辑ID.点播时为视频专辑id(cid),直播时为pid

@property (atomic, strong) NSArray<SPSection *> *sectionArray;             // 要播放的视频url地址列表

@property (nonatomic, assign, readonly) BOOL isPreWatch;                   // 是否试看

@property (atomic, copy) NSArray<SPDefinitionModel *> *defnModelList;      // 清晰度列表

@property (nonatomic, strong) SPDefinitionModel *currentDefinition;        // 当前使用的视频清晰度
@property (nonatomic, strong) SPDefinitionModel *preDefinition;            // 切换前的清晰度，如果未发生切换则为空

@property (nonatomic, assign) BOOL isHevc;

@property (nonatomic, assign) NSInteger vr;                                 // vr,0:无vr，1:普通vr，2:360全集

@property (nonatomic, assign) CGSize videoSize;                             // getVInfo返回的视频宽高(目前直播cgi不会返回，但还是放在基类里面吧)

@property (nonatomic, strong) SPMediaInfo *mediaInfo;                      // TODO:能不能不放这里

@property (nonatomic, assign) SPMediaFormat mediaType;                     // cgi返回的媒体格式

@property (nonatomic, assign, readonly) SPMediaPlayBizType bizType;

@property (nonatomic, copy) NSString *flowId;

/**
 * 截屏/录屏方式 0: 使用 app 逻辑判断
 * 1: 不可截屏/录屏
 * 2: 系统不可但 app 可截屏/录屏
 * 3: 系统和 app 均可截屏/录屏
 */
@property (nonatomic, assign) SPCGISShot sshot;

/**
 * 截图方式 0: 播放器截图，1: 后台截图
 */
@property (nonatomic, assign) NSInteger mshot;

///lowryhe需要新增一个帧率字段
///
///
@property (nonatomic, assign) float frameRate;

@end

