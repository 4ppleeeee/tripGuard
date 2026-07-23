// Copyright 2024 Tencent. All rights reserved.
// Author: mellowxu

#pragma once
#include <stdio.h>
#include <stdbool.h>
#include <stdio.h>
#include "rdelivery/rdelivery_c_define.h"

/**
 封装成 C 接口给 Kotlin/Native 调用
 */
#ifdef __cplusplus
extern "C" {
#endif

typedef void (*OnRequestCallback)(int32_t error_code, int32_t interval);
typedef void (*OnGetDataResultCallback)(int32_t error_code, struct RDData* data);
typedef void (*OnGetDataMapCallback)(int32_t error_code, RDDataMap data_map);
typedef void (*OnCallback)(int32_t error_code);
typedef void (*OnLogCallback)(int level, const char* log);

// 对应 C++ 类的构造和析构函数
void* RDService_Create();
void RDService_Destroy(void* service);

// 初始化 SDK
bool RDService_Init(void* service,
                    const char* db_root_path,
                    struct RDConfig config,
                    OnLogCallback logCallback);

// 按 Key 读取配置和开关
bool RDService_GetRDeliveryDataByKey(void* service,
                                     const char* key,
                                     OnGetDataResultCallback callback);

// 按 Key 读取配置和开关（同步接口）
struct RDData* RDService_SyncGetRDeliveryDataByKey(void* service, const char* key);

// 读取所有配置开关 Map
bool RDService_GetRDeliveryAllDataMap(void* service, OnGetDataMapCallback callback);

// 读取所有配置开关 Map（同步接口）
RDDataMap RDService_SyncGetRDeliveryAllDataMap(void* service);

// 拉取全量配置
bool RDService_RequestFullRemoteData(void* service,
                                     RDKVMap custom_properties,
                                     OnRequestCallback callback);

// 按场景拉取配置
bool RDService_RequestBatchRemoteDataByScene(void* service,
                                             int64_t scene_id,
                                             RDKVMap custom_properties,
                                             OnRequestCallback callback);

// 按多个场景拉取配置
bool RDService_RequestBatchRemoteDataByScenes(void* service,
                                              int64_t scene_ids[],
                                              size_t num_scene_ids,
                                              RDKVMap custom_properties,
                                              OnRequestCallback callback);

// 按单个 Key 拉取配置
bool RDService_RequestSingleRemoteDataByKey(void* service,
                                            const char* key,
                                            RDKVMap custom_properties,
                                            OnRequestCallback callback);

// 按多个 Key 拉取配置
bool RDService_RequestRemoteDataByKeys(void* service,
                                       char* keys[],
                                       size_t num_keys,
                                       RDKVMap custom_properties,
                                       OnRequestCallback callback);

// 添加事件监听者
void RDService_AddEventListener(void* service, RDEventListener* listener);

// 移除事件监听者
void RDService_RemoveEventListener(void* service, RDEventListener* listener);

// 切换用户
bool RDService_SwitchUserId(void* service, const char* user_id, OnCallback callback);

// 切换环境
bool RDService_SwitchEnvironment(void* service, const char* env, OnCallback callback);

// SDK 版本号
const char * RDService_GetSDKVersion(void* service);

#ifdef __cplusplus
}
#endif
