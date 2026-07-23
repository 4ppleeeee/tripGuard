//
//  SPUrlGetterMgr.h
//  SPPlayer
//
//  Created by haitend on 2019/10/14.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPMediaInfo.h"
#import "SPNetVideoInfo.h"
NS_ASSUME_NONNULL_BEGIN

//用于直播请求，区分信息获取目的
typedef NS_ENUM(NSUInteger, SPGetVInfoType) {
    SPGetVInfoTypeGetAddress               = 0,  //获取播放地址
    SPGetVInfoTypePreviewInfo              = 1,  //预获取直播信息,如试看次数等
    SPGetVInfoTypeForDlna                  = 2,  //获取dlna地址
    SPGetVInfoTypeForNetworkSpeedTest      = 3,  //网络测速
};

// 视频信息Delegate，调用方实现此Delegate监听视频信息的回调
@protocol SPUrlGetterDelegate <NSObject>
/**
 * 媒体信息获取完成
 * @param netVideoInfo 媒体信息，详情请见SPNetVideoInfo
 */
- (void)onPlayInfoSuccess:(SPNetVideoInfo *)netVideoInfo requestId:(int)requestId;

/**
 * 媒体信息获取完成
 * @param error 错误信息
 */
- (void)onPlayInfoFailed:(NSError *)error requestId:(int)requestId;

@end



// 请求时需要传入的参数
@interface SPUrlGetterParam : NSObject

@property (nonatomic, strong) SPMediaInfo *mediaInfo;  // 详情请看SPMediaInfo定义。

@property (nonatomic, assign) SPMediaFormat requestMediaFormat;  // 默认为SPMediaFormatAuto

@property (nonatomic, assign) SPGetVInfoType getVinfoType;  //用于直播请求，区分信息获取目的,默认是SPGetProgVInfoTypeAddress

@end

@interface SPUrlGetterMgr : NSObject

@property (nonatomic, weak) id<SPUrlGetterDelegate> delegate;

/** 开始请求，外可以多次调用 */
- (int)startRequestWithParam:(SPUrlGetterParam *)param;
/** 停止请求 */
- (void)stop:(int)requestId;
@end

NS_ASSUME_NONNULL_END
