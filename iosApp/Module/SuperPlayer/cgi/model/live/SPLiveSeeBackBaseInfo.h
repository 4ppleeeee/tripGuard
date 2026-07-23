/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPLiveSeeBackBaseInfo.h
 Author      : charli
 Version     : 1.0
 Date        : 16/11/17
 Description : 直播回看的信息
 History     : 16/11/17 初始版本
 ***********************************************************/

#import <Foundation/Foundation.h>

// 直播回信息，直播回看是指在直播过程中回到之前某个时间点开始播放
// 以下的时间戳用的时都是格林威治时间（1970/1/1 00:00:00）以来的秒数
@interface SPLiveSeeBackBaseInfo : NSObject

//回看开始时间，单位为秒，由server返回
@property (nonatomic, assign) long long seeBackstartTime;

//最大回看时长，单位为秒，由server返回
@property (nonatomic, assign) long long maxSeeBacktime;

//服务器当前时间，单位为秒，由server返回
@property (nonatomic, assign) long long serverTime;

//是否是回看状态，在请求直播回看地址后，设置此字段作为标记
@property (nonatomic, assign) BOOL isSeeBackState;

//用户回看的时间点，即用户拖动到的那个时间点，由客户端计算
@property (nonatomic, assign) long long userSeeBackTime;

//是否可回看
@property (nonatomic, assign) BOOL hasSeeBack;

//直播回看地址
@property (nonatomic, copy) NSString *seeBackUrl;

+ (SPLiveSeeBackBaseInfo *)seeBackInfoWithDict:(NSDictionary *)dict;

@end
