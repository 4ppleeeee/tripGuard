/************************************************************
 Copyright (C), 1998-2018, Tencent Tech. Co., Ltd.
 FileName    : SPProtocolConst.h
 Author      : ethanyxliu
 Version     : 1.0
 Date        : 16/3/30
 Description :
 History     : 16/3/30 初始版本
 ***********************************************************/

#ifndef SPPROTOCOL_CONST_H
#define SPPROTOCOL_CONST_H

/**
 *  在这里定义所有协议用到的常量
 */

//*************** QMF 头常量定义 ******************

static const int gSPQMF_FLAG_UPCOMPRESS = 1 << 0;    //上传压缩标志位
static const int gSPQMF_FLAG_DOWNCOMPRESS = 1 << 1;  //下载压缩标志位
static const int gSPQMF_FLAG_ALGORITHM_SNAPPY = 1 << 2;
static const int gSPQMF_FLAG_ALGORITHM_ZLIB = 1 << 3;
static const int gSPQMF_FLAG_ALGORITHM_GZIP = 1 << 4;  //启用Gzip压缩标志位
static const int gSPQMF_FLAG_PING = 1 << 8;
static const int gSPQMF_FLAG_NEW_PROTOCOL = 1 << 9;
static const int gSPQMF_FLAG_NOSERVICE = 1 << 31;

static const char gSPQMF_MAGIC = 0x13;
static const char gSPQMF_VER = 1;
static const int gSPQMF_CMD = 0xff01;
static const char gSPQMF_ETX = 0x3;

static const char gSPQMF_FIXED_MAGIC = 0x26;
static const char gSPQMF_FIXED_ETX = 0x28;

static const int gSPQMF_MTA_APPID = 1200010169;
static const int gSPQMF_QMF_APPID = 10012;
static const int gSPQMF_APPID = 1000005;
static const char gSPQMF_PLATFORM_IPAD = 4;  // iPad is 4
static const char gSPQMF_QMF_PLATFORM_IPAD = 3;  // iPad QMF Platform is 3

//票据相关
static NSString* gSPQMF_TokenAppID_QQ = @"3000501";
static NSString* gSPQMF_TokenAppID_WX = @"wxcfbccf1c9c3e2a16";

static const char gSPQMF_TokenKeyType_SKEY = 1;       //表示：TokenValue为Skey
static const char gSPQMF_TokenKeyType_LSKEY = 7;      //表示：视频弱登录态，和1定义相同， TokenValue为弱登陆他LSkey
static const char gSPQMF_TokenKeyType_Circle = 9;     //标识：视频圈登录态，视频内部使用的登录态，TokenValue为视频vuserkey
static const char gSPQMF_TokenKeyType_WX = 100;       //标识， 微信票据，sTokenUin表示openid, TokenValue表示accesstoken
static const char gSPQMF_TokenKeyType_WX_CODE = 101;  //标识， 微信sso登录返回的票据

#endif /* SPPROTOCOL_CONST_H */
