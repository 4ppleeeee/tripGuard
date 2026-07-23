//
//  SPLiveQueryBuilder.h
//  SPPlayer
//
//  Created by liyukuan on 2019/11/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIQueryBuilder.h"
#import "SPLiveRequestParam.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPLiveQueryBuilder : SPCGIQueryBuilder

/**
 * 构建直播的query，即url path后面的参数
 * @param paramDict 用来存储query的字典
 * @param liveRequestParam 直播cgi请求参数
*/
+ (void)buildLiveQuery:(NSMutableDictionary *)queryDict liveRequestParam:(SPLiveRequestParam *)liveRequestParam;

/**
 * 存储cgi返回的server时间，下次请求需要带上
 * @param serverTick server时间
 */
+ (void)storeServerTick:(int64_t)serverTick;

@end

NS_ASSUME_NONNULL_END
