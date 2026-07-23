/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : TVKWaterMarkInfo.h
 Author      : charli
 Version     : 1.0
 Date        : 17/2/18
 Description :
 History     : 17/2/18 初始版本
 ***********************************************************/
//

typedef NS_ENUM(NSUInteger, TVKWaterMarkType) {
    TVKWaterMarkTypeImageUrl,  //图片url内容
};

#import <Foundation/Foundation.h>
#import "TVKWaterMarkPosition.h"
#import "TVKRawWaterMarkInfo.h"
#import "SPCGIDefines.h"
// 水印显示所需要的信息
@interface TVKWaterMarkInfo : NSObject

@property (nonatomic, assign) TVKWaterMarkType waterMarkType;  //水印类型

@property (nonatomic, assign) BOOL isShow;  //是否显示

@property (nonatomic, copy) NSString *imageUrl;  //图片url

@property (nonatomic, copy) NSString *imageHttpsUrl;  // https 地址

@property (nonatomic, assign) CGFloat alpha;  //透明度

@property (nonatomic, assign) CGRect originPosition;  //原始参考位置

@property (nonatomic, copy) NSString *MD5;  //水印图片MD5

@property (nonatomic, assign) int rw;  //原始参考位置缩放系数，请见http://tapd.oa.com/qqvideo_prj/markdown_wikis/#1010114481006415665

- (id)initWithWaterMarkMD5:(NSString *)MD5
                  imageUrl:(NSString *)imageUrl
             imageHttpsUrl:(NSString *)imageHttpsUrl
            originPosition:(CGRect)originPosition
                     alpha:(CGFloat)alpha
                    isShow:(BOOL)isShow
                        rw:(int)rw;
@end

@interface TVKWaterMarkCGIInfo : NSObject

@property (nonatomic, strong) TVKWaterMarkModel *waterMarkModel;

@property (nonatomic, assign) CGSize videoSize;

@property (nonatomic, assign) SPMediaPlayBizType bizType;

@end

@interface TVKWaterMarkExtraInfo : NSObject

@property (nonatomic, assign) CGSize videoSize;

@property (nonatomic, assign) NSTimeInterval position;

@property (nonatomic, assign) SPVideoStretchMode stretchMode;

@end
