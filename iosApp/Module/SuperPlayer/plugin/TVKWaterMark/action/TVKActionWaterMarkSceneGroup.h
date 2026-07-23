/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKActionWaterMarkSceneGroup.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/8/24
 Description :
 History     : 2017/8/24 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"

@class TVKActionWaterMarkModel;

/*
 * 管理一组动态水印scene的显示，每个scene包含若干张水印图片，这几张水印图片按照一定的时间间隔轮换展示。scene的含义和显示逻辑请参考TVKActionWaterMarkSceneDisplay.h
 * 动态水印显示有两种模式，一种是按照播放位置进行展示，一种是按照从开始播放的相对时间(elapse)来展示，当处于这种显示模式时，每个scene的水印图片是随着时间的变化而轮换展示的。
 * 动态水印显示模式请见TVKActionWaterMarkModel的runMode属性。
 */
@interface TVKActionWaterMarkSceneGroup : NSObject

/**
 * 初始化一个TVKActionWaterMarkSceneGroup实例
 * @param model 动态水印model，是控制动态水印显示的参数
 * @param container 动态水印显示的容器
 * @param videoSize 视频大小
 * @return 一个TVKActionWaterMarkSceneGroup实例
 */
- (instancetype)initWithActionWaterMarkModel:(TVKActionWaterMarkModel *)model container:(UIView *)container videoSize:(CGSize)videoSize;

/**
 * 设置当前播放位置，当动态水印按照播放位置展示时，调用此方法设置播放位置
 * @param position 当前播放位置，单位为秒
 */
- (void)setPlayPosition:(NSTimeInterval)position;

/**
 * 设置当前播放位置，当动态水印从开始播放的相对时间来展示时，调用此方法设置相对时间
 * @param time 相对于播放开始的时间，即从播放开始过了多长时间，单位为秒
 */
- (void)setRelativeTime:(NSTimeInterval)time;

/**
 * 设置视频拉伸模式
 * @param mode 视频拉伸模式
 */
- (void)setStretchMode:(SPVideoStretchMode)mode;

/**
 * 视频view大小发生变化时调用此方法
 * @param videoViewSize 视频view大小
 */
- (void)onVideoViewSizeChanged:(CGSize)videoViewSize;

/**
 * 视频大小发生变化时调用此方法
 * @param videoSize 视频大小
 */
- (void)onVideoSizeChange:(CGSize)videoSize;

/** 刷新水印UI */
- (void)requestLayout;

/**
 * 不再使用，释放资源
 */
- (void)destroy;
@end
