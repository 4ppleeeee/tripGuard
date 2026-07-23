/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkPosition.h
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/
//

#import <Foundation/Foundation.h>
#import "SPPlayerDefine.h"

// 用于计算水印位置
@interface TVKWaterMarkPosition : NSObject

@property (nonatomic, assign) CGSize videoSize;  //视频大小

@property (nonatomic, assign) CGRect originPosition;  //水印原始参考位置

@property (nonatomic, assign) CGSize viewSize;  //视频view大小

@property (nonatomic, assign) SPVideoStretchMode stretchMode;  //视频拉伸模式

@property (nonatomic, assign) int rw;  // 如无该值则填0，默认为0

- (CGRect)waterMarkPosition;

@end
