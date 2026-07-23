/*****************************************************************************
 * @copyright Copyright (C), 1998-2019, Tencent Tech. Co., Ltd.
 * @file     ISPPlayInfoGetter.h
 * @brief    cgi请求接口
 * @author   hemanli
 * @version  1.0.0
 * @date     2019/9/21
 * @license  GNU General Public License (GPL)
 *****************************************************************************/

#import <Foundation/Foundation.h>
#import "SPCGIRequestParam.h"
#import "SPMediaPlayInfo.h"

@protocol ISPPlayInfoGetter;

@protocol ISPPlayInfoGetterDelegate <NSObject>

@optional

/**
 * cgi成功返回时调用该方法
 * @param playInfo 如果是点播，playInfo为SPVODPlayInfo的一个实例，如果是直播，playInfo为SPLivePlayInfo的一个实例
 */
- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onGetPlayInfo:(SPMediaPlayInfo *)playInfo playID:(int)playID;

- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onPlayInfoUpDate:(SPMediaPlayInfo *)playInfo playID:(int)playID;

- (void)playInfoGetter:(id<ISPPlayInfoGetter>)getter onGetPlayInfoFailedWithError:(NSError *)error playID:(int)playID;

@end

@interface SPCGIInitParam : NSObject

@property (nonatomic, copy) NSString *logTag;

@end

@protocol ISPPlayInfoGetter <NSObject>

@required

@property (nonatomic, weak) id<ISPPlayInfoGetterDelegate> delegate;

- (instancetype)initWithParam:(SPCGIInitParam *)param;

- (int)requestWithParam:(SPCGIRequestParam *)param;

- (void)stopWithPlayID:(int)playID;

@end
