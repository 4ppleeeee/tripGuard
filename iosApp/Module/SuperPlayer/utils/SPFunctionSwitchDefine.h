/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPFunctionSwitchDefine.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 17/4/10
 Description : 功能宏开关类
 History     : 17/4/10 初始版本
 ***********************************************************/

/** 功能宏开关 */

// appkey是否鉴权并使用测试的platform
#define SP_AUTH_TEST 0

// 往外抛出播放数据
#define SP_CAST_PLAY_DATA 1

// 配置拉取是否走测试环境
#define CONFIG_TEST 0

//直播走p2p测试用
#define LIVE_P2P_DEBUG 0

// vinfo是否使用测试环境
#define SP_VINFO_TEST 0

// 直播请求是否使用测试环境
#define SP_LIVE_VINFO_TEST 0

//是否是sdk， sdk情况下会有一些功能要打开.也会更改ODK的上报的appkey。所以作为SDK的时候要设置为1
#define SP_TARGET_PLAYER_SDK 0

//是否是腾讯视频内部应用，部分功能仅对腾讯视频内部应用开放
#define SP_TARGET_PLAYER_INNER_APP 1

//是否是腾讯视频iphone版本。也会更改ODK的上报的appkey。所以在腾讯视频iPhone上使用的时候，一定要设置为1
#define SP_TARGET_PLAYER_IPHONE 1

//是否是腾讯视频ipad版本。也会更改ODK的上报的appkey。所以在腾讯视频iPad上使用的时候，一定要设置为1
#define SP_TARGET_PLAYER_IPAD 0
