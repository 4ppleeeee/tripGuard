// Copyright 2021 Tencent. All rights reserved.
// Author: arlowang

#pragma once
#include "core/type_define.h"

#ifdef __cplusplus
#include <cstdint>
extern "C" {
#endif

// 配置定时更新默认时间间隔，单位s
static int32_t kDefaultUpdateInterval = 4 * 60 * 60;
// 配置定时更新最小时间间隔，单位s
static int32_t kMinUpdateInterval = 10 * 60;

enum RDUpdateStrategy {
    RDUpdateStrategySdkInit = 1,         // sdk初始化时更新
    RDUpdateStrategySchedual = 1 << 1,   // 定时更新
    RDUpdateStrategyEnterForceground = 1 << 2,  // 热启动更新（退后台超过30s切回前台）
    RDUpdateStrategyNetworkReconnect = 1 << 3  // 断网重连时更新
};

// 开关配置数据
struct RDData {
    // 配置名称
    const char* key;
    // 配置内容类型
    enum RDValueType value_type;
    // 配置内容
    const char* value;
    // 开关状态
    enum RDSwitch switch_state;
    // 其他信息
    // Report report;
    // 调试信息(任务 id)
    const char* debugInfo;
};

// 定义一个结构来表示 map 中的单个键值对
typedef struct {
    char* key;        // 字符串 key 被表示为一个 char 指针
    struct RDData value;     // value 是一个指向 RDData 结构的指针
} RDDataPair;

typedef struct {
    RDDataPair* pairs;   // 指向 RDDataPair 数组的指针
    int size;
} RDDataMap;

typedef struct {
    char* key;
    char* value;
} RDKVPair;

typedef struct {
    RDKVPair* pairs;
    int size;
} RDKVMap;

struct RDConfig {
  /* PullRequest */
  // 应用鉴权,用于生成sign
  char* app_key;
  // 系统ID
  int32_t system_id;
  // 拉取目标：资源是1按app拉取，配置是0按项目拉取
  int32_t target;
  // 应用编号
  char* app_id;
  // 逻辑环境，主要是资源下载系统会使用
  // 逻辑环境。""(空字符串)为正式环境，"1"为测试环境，其他逻辑环境参照管理端页面填写。
  char* logic_environment;

  /* PullParams */
  // 业务方自定义属性

  /* Properties */
  // 平台
  enum RDPlatform platform;
  // 语言
  char* language;
  // SDK版本号
  char* sdk_version;
  // 用户id（对应协议中的 guid）
  char* user_id;
  // 设备id（对应协议中的 qimei）
  char* device_id;
  // 宿主App版本号
  char* app_version;
  // 操作系统版本号
  char* os_version;
  // 应用包名
  char* bundle_id;
  // 业务方设置的命中后不再更新的配置开关key集合。
  // 对于此集合中的key,当调用配置值查询或者开关值查询接口成功查到值后，
  // 在sdk的当前生命周期内，即使后续前端更新了配置值或者开关值，调用配置值查询或者开关值查询接口查到的值都不会变化。
//  int fixed_after_hit_keys_size; // 设置 fixed_after_hit_keys 时，必须同时告知数组大小
//  char* fixed_after_hit_keys[];
};

typedef struct RDEventListener {
    void (*OnDataInitComplete)(int32_t error_code);
    void (*OnDataAdd)(const char* key, struct RDData* data);
    void (*OnDataChange)(const char* key, struct RDData* old_data, struct RDData* new_data);
    void (*OnDataDelete)(const char* key);
} RDEventListener;

typedef struct ListenerNode {
    RDEventListener* listener;
    struct ListenerNode* next;
} ListenerNode;

// 创建 RDKVPair
RDKVPair RDCreateKVPair(const char* key, const char* value);

// 销毁 RDKVPair
void RDDestoryKVPair(RDKVPair* pair);

// 创建 RDKVMap
void RDCreateKVMap(RDKVMap* map, int size);

// 销毁 RDKVMap
void RDDestoryKVMap(RDKVMap* map);

#ifdef __cplusplus
} // extern "C"
#endif
