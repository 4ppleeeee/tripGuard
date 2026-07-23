/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKActionWaterMarkSceneDisplay.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/8/24
 Description :
 History     : 2017/8/24 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"

@class TVKActionWaterMarkScene;
@class TVKWaterMarkViewFactory;

// 负责动态水印一个scene的显示，一个scene的显示是一组图片的的轮换展示。
// 动态水印的格式如下
/**
 * 动态水印展示逻辑
 * description 一个动态水印的格式如下
 *             {
 *               "duration":15000,
 *               "start":0,
 *               "rw":1080,
 *               "repeat":0,
 *               "scenes":
 *               [
 *                 {
 *                   in":0,
 *                   "out":5000,
 *                   "wi":[{"id":19,"x":54,"y":54,"w":336,"h":108,"a":100,"md5":"dcc9dc5c478c4100ea2817c5e6020f26",
 *                          "url":"http://puui.qpic.cn/vcolumn_pic/0/logo_qing_xi_color_336_108.png/0"}]
 *                 },
 *                 {
 *                   "in":7000,
 *                   "out":12000,
 *                   "wi":[{"id":6,"x":54,"y":54,"w":336,"h":108,"a":100,"md5":"2988fe549d3156a8a6b95152f46ced29",
 *                          "url":"http://puui.qpic.cn/vcolumn_pic/0/logo_qing_xi_test.png/0"}]
 *                 }
 *               ]
 *             }
 *             scenes字段包含若干个scene，一个scene包含一组水印（数据结构是一个数组，但实际往往只返回一个），
 *             在播放过程中，scene可能是周期性显示的，比如duration是15s，则每个周期15s，每个scene在一个周期内有自己显示和消失的时间点（即in和out），这个时间点不是时钟时间，而是播放器进度条的position。
 *             TVKActionWaterMarkSceneDisplay不关心播放position在什么位置，只关心当前处于某个周期的那个时间点，外面通过setTimePoint:把时间点传进来
 */
@interface TVKActionWaterMarkSceneDisplay : NSObject

@property (nonatomic, strong) TVKWaterMarkViewFactory *factory;  // 有外面设置进来，用来创建TVKWaterMarkView

@property (nonatomic, strong, readonly) TVKActionWaterMarkScene *actionScene;  // 动态水印scene数据，只读，在初始化时传入。

/**
 * 初始化一个TVKActionWaterMarkSceneDisplay实例
 * @param actionScene 动态水印scene数据
 * @param container 动态水印显示的容器
 * @param videoSize 视频大小
 * @param rw 水印参考位置的缩放值，使用方法请见http://tapd.oa.com/qqvideo_prj/markdown_wikis/#1010114481006415665
 */
- (instancetype)initWithWaterMarkScene:(TVKActionWaterMarkScene *)actionScene
                             container:(UIView *)container
                             videoSize:(CGSize)videoSize
                                    rw:(int)rw;

/**
 * 设置一个显示周期内的时间点
 * @param timePoint 内部根据timePoint计算是否显示，但如果传负值，则不显示
 */
- (void)setTimePoint:(int)timePoint;

/**
 * 设置拉伸模式
 * @param mode 拉伸模式
 */
- (void)setStretchModel:(SPVideoStretchMode)mode;

/**
 * 当video view大小发生改变时调用此方法
 * @param videoViewSize 新的video view大小
 */
- (void)onVideoViewSizeChanged:(CGSize)videoViewSize;

/**
 * 当视频大小发生改变时调用此方法
 * @param videoSize 新的视频大小
 */
- (void)onVideoSizeChanged:(CGSize)videoSize;

/** 刷新水印UI */
- (void)requestLayout;

@end
