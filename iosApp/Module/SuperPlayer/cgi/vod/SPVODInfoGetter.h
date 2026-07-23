/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPVODInfoGetter.h
 * @brief    点播cgi请求实现
 * @author   hemanli
 * @version  1.0.0
 * @date     2019/9/21
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>
#import "ISPPlayInfoGetter.h"
#import "SPCGIBase.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPVODInfoGetter : SPCGIBase <ISPPlayInfoGetter>

- (instancetype)init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
