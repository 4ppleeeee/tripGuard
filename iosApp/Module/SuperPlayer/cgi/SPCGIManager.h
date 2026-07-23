/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     SPCGIManager.h
 * @brief    CGI请求管理类，统一管理点播和直播cgi的请求
 * @author   hemanli
 * @version  1.0.0
 * @date     2019/9/12
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>
#import "SPMediaPlayInfo.h"
#import "SPPlayParam.h"
#import "SPPlayCommonParam.h"
#import "SPPlayerBase.h"

NS_ASSUME_NONNULL_BEGIN

@class SPCGIManager;

@protocol SPCGIManagerDelegate <NSObject>

@optional

- (void)cgiManagerOnGetPlayInfo:(SPMediaPlayInfo *)playInfo requestParam:(SPPlayParam *)requestParam;

- (void)cgiManagerOnPlayInfoUpdate:(SPMediaPlayInfo *)playInfo requestParam:(SPPlayParam *)requestParam;

- (void)cgiManagerOnError:(NSError *)error requestParam:(SPPlayParam *)requestParam;

@end

@interface SPCGIManager : SPPlayerBase

- (instancetype)initWithParam:(SPPlayCommonParam *)param;

@property (nonatomic, weak) id<SPCGIManagerDelegate> delegate;

- (void)requestWithPlayParam:(SPPlayParam *)playParam;

- (void)cancel;

@end

NS_ASSUME_NONNULL_END
