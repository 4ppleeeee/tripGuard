// Copyright 2021 Tencent. All rights reserved.
// Author: arlowang

#ifndef FRONTEND_QQNT_KERNEL_COMMON_RDELIVERY_TYPE_DEFINE_H_
#define FRONTEND_QQNT_KERNEL_COMMON_RDELIVERY_TYPE_DEFINE_H_

#include <stdio.h>

/* 对于单个请求或者全量请求，后台不会返回没有变化的配置列表；
 * 只有根据场景id批量请求时才会返回没有变化的配置列表；*/
enum RDPullType {
    RDPullTypeUnknown = 0,
    RDPullTypeDeprecated1 = 1,  // 废弃序号，请勿使用
    RDPullTypeGroup = 2,  // 按分组拉取
    RDPullTypeConfig = 3,  // 按单个配置拉取
    RDPullTypeAll = 4  // 全量拉取
};

enum RDValueType {
    RDValueTypeString = 0,   // STRING
    RDValueTypeJson = 1,     // JSON
    RDValueTypeInt = 2,      // Int
    RDValueTypeBool = 3,     // Bool
    RDValueTypeFloat = 4,    // Float
    RDValueTypeList = 5,     // List
    RDValueTypeMap = 6,      // Map
};

enum RDPlatform {
    RDPlatformUndefined = -1,
    RDPlatformUnknown = 0,  // UNKNOWN
    RDPlatformCommon = 1,  // COMMON
    RDPlatformAndroid = 2,  // ANDROID
    RDPlatformIos = 3,  // IOS
    RDPlatformWeb = 4,  // WEB
    RDPlatformServer = 5,  // SERVER
    RDPlatformMicroApp = 6,  // 小程序
    RDPlatformIPad = 7,
    RDPlatformAndroidPad = 8,
    RDPlatformWindows = 9,
    RDPlatformMac = 10,
    RDPlatformNodeServer = 11,
    RDPlatformVisionOS = 12,
    RDPlatformHarmony = 13,    // 鸿蒙
};

enum RDSwitch {
    RDSwitchNoSwitch = 0,  // 非开关
    RDSwitchOn = 1,  // 开
    RDSwitchOff = 2  // 关
};

enum RDSystemId {
    RDSystemIdConfig = 10001,
    RDSystemIdResHub = 10010,
    RDSystemIdTAB    = 10013,
    RDSystemIdAppRelease = 10016,
    RDSystemIdFix    = 10021,
};

enum RDPullTarget {
    RDPullTargetProject = 0,
    RDPullTargetApp = 1,
};

// 正式环境
static const char* kLogicEnvironmentProduct = "";
// 测试环境
static const char* kLogicEnvironmentDevelopment = "1";

#endif  // FRONTEND_QQNT_KERNEL_COMMON_RDELIVERY_TYPE_DEFINE_H_
