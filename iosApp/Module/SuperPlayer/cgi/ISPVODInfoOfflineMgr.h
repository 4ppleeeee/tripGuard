//
//  ISPVODInfoOfflineMgr.h
//  SPPlayer
//
//  Created by liyukuan on 2019/10/30.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPMediaInfo.h"
#import "SPVODPlayInfo.h"

@interface SPVODInfoOfflineRequestParam : NSObject

@property (nonatomic, strong) SPMediaInfo *mediaInfo;

@property (nonatomic, assign) SPMediaFormat mediaFormat;

@property (nonatomic, copy) NSString *offlineSdtfrom;

/** 调用者自定义的一个值，底层不理解它的含义，在SPVODInfoOfflineCompletion返回时，
    会将SPVODInfoOfflineRequestParam的实例以及这个值带回给调用者。调用者可以用它来区分对应的
    是哪一次requestWithMediaInfo调用
 */
@property (nonatomic, strong) id Opaque;

@end

typedef void (^SPVODInfoOfflineCompletion)(SPVODInfoOfflineRequestParam *requestParam, SPVODPlayInfo *vodPlayInfo, NSError *error);

/**
 * 专为下载用的cgi请求接口
 */
@protocol ISPVODInfoOfflineMgr <NSObject>

/**
 * 发起视频信息获取请求。同一个实例，在completion返回之前，不可再发起请求，否则会取消之前的请求。
 * 如果需要同时请求多个实例，建议通过SPFactory构建多个实例。
 */
- (void)requestWithMediaInfo:(SPVODInfoOfflineRequestParam *)requestParam completion:(SPVODInfoOfflineCompletion)completion;
/** 取消当前的请求 */
- (void)cancel;

@end
