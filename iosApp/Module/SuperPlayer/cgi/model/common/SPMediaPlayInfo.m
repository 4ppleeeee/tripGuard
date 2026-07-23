/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPMediaPlayInfo.m
 * @brief    播放所需信息，主要是由CGI返回的信息
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPMediaPlayInfo.h"
#import "SPVODPlayInfo.h"
#import "SPLivePlayInfo.h"

@implementation SPMediaPlayInfo

- (SPMediaPlayBizType)bizType {
    if ([self isKindOfClass:[SPVODPlayInfo class]]) {
        return SPMediaPlayBizTypeVod;
    } else {
        return SPMediaPlayBizTypeLive;
    }
}

@end
