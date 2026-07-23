/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : QLWaterMarkFactory.h
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>
#import "TVKWaterMarkView.h"

@interface TVKWaterMarkViewFactory : NSObject

- (TVKWaterMarkView *)createWaterMarkView:(TVKWaterMarkInfo *)waterMarkInfo;

- (TVKWaterMarkView *)queryWaterMarkView:(TVKWaterMarkInfo *)waterMarkInfo;

- (TVKWaterMarkView *)removeWaterMarkViewWithInfo:(TVKWaterMarkInfo *)waterMarkInfo;

- (TVKWaterMarkView *)getWaterMarkView:(TVKWaterMarkInfo *)waterMarkInfo;

/**
 * 移除所有的水印，并释放水印占用的资源
 */
- (void)removeAll;
@end
