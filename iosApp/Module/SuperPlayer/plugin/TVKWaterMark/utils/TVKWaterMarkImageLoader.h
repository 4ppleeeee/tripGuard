/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkImageLoader.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 2017/12/19
 Description : 水印图片下载器
 History     : 2017/12/19 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

// 回调block
typedef void (^TVKWaterMarkImageLoaderCompletionBlock)(UIImage *image, NSError *error);

@interface TVKWaterMarkImageLoader : NSObject

/*
 * 从url中加载图片，首先检查本地缓存，如果本地缓存有，则直接回调completion，如果没有，则发送网络请求
 * @param urlStr url字符串
 * @param MD5 图片MD5（后台返回）
 * @param completeion 回调block，回调会在主线程执行
 */
- (void)loadWithUrl:(NSString *)urlStr
                MD5:(NSString *)MD5
         completion:(TVKWaterMarkImageLoaderCompletionBlock)completion;

@end
