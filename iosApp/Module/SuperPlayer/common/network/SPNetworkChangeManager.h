/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : QLAppNetworkManager.h
 Author      : deronhuang
 Version     : 1.0
 Date        : 2017/1/3
 Description :
 History     : 2017/1/3 初始版本
 ***********************************************************/
/**
 *  @author deronhuang, 17-01-03 11:01:26
 *
 *  appdelegate处理网络相关的问题
 */
#import <Foundation/Foundation.h>

@interface SPNetworkChangeManager : NSObject

+ (instancetype)sharedInstance;

@end
