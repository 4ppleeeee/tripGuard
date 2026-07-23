/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPLivePlayInfo.m
 * @brief    直播播放所需信息，主要是由CGI返回的信息
 * @author   ethanyxliu
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import "SPLivePlayInfo.h"

@implementation SPLivePlayInfo

- (BOOL)isPreWatch {
    return (self.needPay &&
            !self.isUserPay &&
            self.livePlayTime > 0 &&
            self.livePreviewTime > 0);
}


@end
