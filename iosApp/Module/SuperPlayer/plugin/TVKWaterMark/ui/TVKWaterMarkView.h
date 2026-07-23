/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMark.h
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "TVKWaterMarkInfo.h"
#import "SPPlayerDefine.h"

// 水印view，里面包含一个UIImageView，用来展示水印
@interface TVKWaterMarkView : NSObject

@property (nonatomic, strong) TVKWaterMarkInfo *waterMarkInfo;  //水印显示所用的信息

@property (nonatomic, strong) UIView *container;  //水印的容器，即父view

/**
 * 初始化一个TVKWaterMarkView
 * @param waterMarkInfo 水印信息
 * @return 一个TVKWaterMarkView实例
 */
- (instancetype)initWithWaterMarkInfo:(TVKWaterMarkInfo *)waterMarkInfo;

/**
 * 显示水印
 */
- (void)show;

/**
 * 隐藏水印
 */
- (void)hide;

/**
 * 设置视频view大小
 * @param videoViewSize 视频view大小
 */
- (void)setVideoViewSize:(CGSize)videoViewSize;

/**
 * 设置视频大小
 * @param videoSize 视频大小
 */
- (void)setVideoSize:(CGSize)videoSize;

/**
 * 设置拉伸模式
 * @param mode 拉伸模式，请见SPVideoStretchMode定义
 */
- (void)setStretchMode:(SPVideoStretchMode)mode;

/**
 * 刷新水印UI
 */
- (void)requestLayout;

/**
 * 销毁对象，释放内部资源
 */
- (void)destroy;

@end
