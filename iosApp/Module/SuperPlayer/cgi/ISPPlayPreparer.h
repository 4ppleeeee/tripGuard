//
//  ISPPlayPreparer.h
//  SuperPlayer
//
//  Created by liyukuan on 2019/10/23.
//  Copyright © 2019 tencent. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"

@class SPMediaInfo;
@class SPNetVideoInfo;
@class SPUserInfo;

//用于直播请求，区分信息获取目的
typedef NS_ENUM(NSUInteger, SPGetProgVInfoType) {
    SPGetProgVInfoTypeAddress = 0,      //获取播放地址
    SPGetProgVInfoTypePreviewInfo = 1,  //预获取直播信息,如试看次数等
};

// 请求时需要传入的参数
@interface SPPlayPreparerParam : NSObject

@property (nonatomic, strong) SPMediaInfo *mediaInfo;  // 详情请看SPMediaInfo定义。

@property (nonatomic, assign) BOOL isDlna;  // 是否是Dlna，默认为NO

@property (nonatomic, assign) BOOL needP2P;  // 是否走P2P，是否走P2P，但内部还会判断是否可以走P2P，所以最后不一定走P2P。

@property (nonatomic, assign) SPMediaFormat requestMediaFormat;  // 默认为SPMediaFormatAuto

@property (nonatomic, strong) SPUserInfo *userInfo;  // 非必须，可选

@property (nonatomic, assign) SPGetProgVInfoType getProgVinfoType;  //用于直播请求，区分信息获取目的,默认是SPGetProgVInfoTypeAddress

@end

// 视频信息Delegate，调用方实现此Delegate监听视频信息的回调
@protocol SPPlayPreparerDelegate <NSObject>
/**
 * 媒体信息获取完成
 * @param netVideoInfo 媒体信息，详情请见SPNetVideoInfo
 */
- (void)onMediaInfoPrepared:(SPNetVideoInfo *)netVideoInfo;

/**
 * 媒体信息获取完成
 * @param error 错误信息
 */
- (void)onMediaInfoPrepareFailed:(NSError *)error;

@end

@protocol ISPPlayPreparer <NSObject>

@property (nonatomic, weak) id<SPPlayPreparerDelegate> delegate;

/**
 * 调用该方法开始获取媒体信息
 * @param param 获取媒体信息需要的参数，请见SPPlayPreparerParam。
 */
- (void)startPrepareWithParam:(SPPlayPreparerParam *)param;

/**
 * 停止获取媒体信息，如果在开始获取信息后想停止，可以调用此方法，就不会收到回调。
 */
- (void)stop;

@end
