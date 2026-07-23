/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPCertificateMgr.h
 Author      : liyukuan
 Version     : 1.0
 Date        : 2018/1/13
 Description : 视频加密证书管理
 History     : 2018/1/13 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

typedef void (^TVKCertificateCompletionBlock)(NSData *cerData, NSError *error);

@interface SPCertificateMgr : NSObject

+ (SPCertificateMgr *)sharedInstance;

- (void)getCertificateWithUrl:(NSString *)url completion:(TVKCertificateCompletionBlock)completion;

@end
