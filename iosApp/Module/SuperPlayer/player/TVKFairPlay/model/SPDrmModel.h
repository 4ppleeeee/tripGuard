/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName: SPVInfoModels.h
 Author: liyukuan
 Version : 1.0
 Date: 2018/1/13
 Description:
 
 History: 2018/1/13 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

@interface SPDrmModel : NSObject

@property (nonatomic, assign) int drm;  // drm类型, 1:普通drm 2:伪drm 4:数字太和drm 8:hls加密 16:fairplay加密

@property (nonatomic, copy) NSString *ckcUrl;  // getckc url

@property (nonatomic, copy) NSString *cerUrl;  // 证书地址

@property (nonatomic, strong) NSData *cerData;  // 证书内容

+ (SPDrmModel *)drmModelByParseCKCField:(NSString *)ckc drm:(int)drm;

@end
