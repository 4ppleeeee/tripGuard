//
//  SPVODQueryBuilder.h
//  SPPlayer
//
//  Created by liyukuan on 2019/11/2.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPCGIQueryBuilder.h"
#import "SPVODRequestParam.h"

NS_ASSUME_NONNULL_BEGIN

@interface SPVODQueryBuilder : SPCGIQueryBuilder

/**
 * 构建点播的query，即url path后面的参数
 * @param queryDict 用来存储query的字典
 * @param vodRequestParam  点播请求参数
*/
+ (void)buildVODQuery:(NSMutableDictionary *)queryDict vodRequestParam:(SPVODRequestParam *)vodRequestParam;

/**
 * 构建点播通用的query，比如平台号、系统版本号、网络类型
 * @param paramDict 用来存储query的字典
 * @param requestCommonParam 一个SPCGIRequestCommonParam的实例
 */
+ (void)buildVODCommonParam:(NSMutableDictionary *)paramDict requestCommonParam:(SPCGIRequestCommonParam *)requestCommonParam;

@end

NS_ASSUME_NONNULL_END
