/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkUtil.h
 Author      : hemanli
 Version     : 1.0
 Date        : 2017/8/28
 Description :
 History     : 2017/8/28 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

@class TVKWaterMarkInfo;
@class TVKRawWaterMarkInfo;
@class TVKVODWaterMarkInfo;
@class TVKLiveWaterMarkInfo;

// 水印帮助函数
@interface TVKWaterMarkUtil : NSObject
/**
 * 从后台返回的水印信息构造显示所要的信息
 * @param waterMarkInfoArray 后台返回的水印信息列表
 * @param rw 水印原始参考位置的缩放系数，请见http://tapd.oa.com/qqvideo_prj/markdown_wikis/#1010114481006415665
 * @return 一个TVKWaterMarkInfo列表
 */
+ (__kindof NSArray<TVKWaterMarkInfo *> *)waterMarkDisplayInfosFromWaterMarkInfos:(__kindof NSArray<TVKRawWaterMarkInfo *> *)waterMarkInfoArray
                                                                               rw:(int)rw;

/**
 * 从后台返回的水印信息构造显示所要的信息
 * @param vodWaterMarkInfo 后台返回的点播水印信息
 * @param rw 水印原始参考位置的缩放系数，请见http://tapd.oa.com/qqvideo_prj/markdown_wikis/#1010114481006415665
 * @return 一个TVKWaterMarkInfo实例
 */
+ (TVKWaterMarkInfo *)waterMarkDisplayInfoFromVODWaterMarkInfo:(TVKVODWaterMarkInfo *)vodWaterMarkInfo rw:(int)rw;

/**
 * 从后台返回的水印信息构造显示所要的信息
 * @param liveWaterMarkInfo 后台返回的直播水印信息
 * @param rw 水印原始参考位置的缩放系数，请见http://tapd.oa.com/qqvideo_prj/markdown_wikis/#1010114481006415665
 * @return 一个TVKWaterMarkInfo实例
 */
+ (TVKWaterMarkInfo *)waterMarkDisplayInfoFromLiveWaterMarkInfo:(TVKLiveWaterMarkInfo *)liveWaterMarkInfo rw:(int)rw;
@end
