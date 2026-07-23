//
// Created on 2024/10/30.
//
// Node APIs are not fully supported. To solve the compilation error of the interface cannot be found,
// please include "napi/native_api.h".

#ifndef BUGLY_HARMONY_TDLOG_INTERFACE_H
#define BUGLY_HARMONY_TDLOG_INTERFACE_H

#define OHOS_TDLOG_LEVEL_ALL 0
#define OHOS_TDLOG_LEVEL_VERBOSE 0
#define OHOS_TDLOG_LEVEL_DEBUG 1
#define OHOS_TDLOG_LEVEL_INFO 2
#define OHOS_TDLOG_LEVEL_WARNING 3
#define OHOS_TDLOG_LEVEL_ERROR 4
#define OHOS_TDLOG_LEVEL_FATAL 5
#define OHOS_TDLOG_LEVEL_NONE 6

#undef LOG_DOMAIN
#undef LOG_TAG
#define LOG_DOMAIN 0x0000
#define LOG_TAG "Bugly-Logger"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * 打印日志
 * @param namePrefix 日志实例前缀，主实例直接填 nullptr，子实例填对应的前缀名
 * @param level 日志级别
 * @param tag 日志tag
 * @param log 日志内容
 */
__attribute__((visibility("default"))) void TDLogPrintLog(const char* namePrefix, int level, const char* tag, const char* log);

/**
 * 刷新日志
 * @param namePrefix 日志实例前缀，主实例直接填 nullptr，子实例填对应的前缀名
 */
__attribute__((visibility("default"))) void TDLogFlushLog(const char* namePrefix);

#ifdef __cplusplus
}
#endif

#endif //BUGLY_HARMONY_TDLOG_INTERFACE_H
