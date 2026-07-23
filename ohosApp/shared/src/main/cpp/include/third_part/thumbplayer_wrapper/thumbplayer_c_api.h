/**
 * Copyright (c) 2024 Tencent. All rights reserved.
 *
 * ThumbPlayer C API 桥接层
 * 将 ThumbPlayer C++ 核心接口扁平化为纯 C 接口，
 * 供 Kotlin/Native cinterop 工具生成 Kotlin 绑定。
 *
 * @author codebuddy
 * @date 2026/03/19
 */

#ifndef THUMBPLAYER_C_API_H
#define THUMBPLAYER_C_API_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* ======================== 不透明句柄类型 ======================== */

/** 播放器句柄 */
typedef void* TPPlayerHandle;

/** MediaAsset 句柄 */
typedef void* TPMediaAssetHandle;

/** 预加载器句柄 */
typedef void* TPPreloaderHandle;

/** MediaAssetRequest 句柄（换源回调中使用） */
typedef void* TPMediaAssetRequestHandle;

/* ======================== C 结构体定义 ======================== */

/** 错误信息结构体 */
typedef struct {
    int error_type;
    int error_code;
    int64_t current_pos_ms;
} TPCError;

/** OnInfo 参数结构体 */
typedef struct {
    int64_t long_param1;
    int64_t long_param2;
    float float_param1;
    float float_param2;
    const char* str_param1;
    const char* str_param2;
    /** OnInfo 子对象类型标识：0=无, 1=TPCDownloadProgressInfo, 2=TPCVideoSeiInfo */
    int obj_type;
    void* obj_param;
} TPCOnInfoParam;

/** 下载进度信息结构体 */
typedef struct {
    int available_position_ms;
    int download_speed_bps;
    int download_bytes;
    int file_total_bytes;
    const char* extra_info;
} TPCDownloadProgressInfo;

/** 视频 SEI 信息结构体 */
typedef struct {
    int codec_type;
    int sei_type;
    const char* sei_data;
    int sei_data_len;
} TPCVideoSeiInfo;

/* ======================== 回调函数指针类型定义 ======================== */

/** OnPrepared 回调 */
typedef void (*tp_on_prepared_callback)(void* context);

/** OnCompletion 回调 */
typedef void (*tp_on_completion_callback)(void* context);

/** OnError 回调 */
typedef void (*tp_on_error_callback)(void* context, const TPCError* error);

/** OnInfo 回调 */
typedef void (*tp_on_info_callback)(void* context, int on_info_id, const TPCOnInfoParam* param);

/** OnSeekComplete 回调 */
typedef void (*tp_on_seek_complete_callback)(void* context, int64_t opaque);

/** OnVideoSizeChanged 回调 */
typedef void (*tp_on_video_size_changed_callback)(void* context, int width, int height);

/** OnStateChanged 回调 */
typedef void (*tp_on_state_changed_callback)(void* context, int pre_state, int cur_state);

/** OnStopAsyncComplete 回调 */
typedef void (*tp_on_stop_async_complete_callback)(void* context);

/** OnMediaAssetExpire 回调 */
typedef void (*tp_on_media_asset_expire_callback)(void* context, TPMediaAssetRequestHandle request);

/** 日志回调 */
typedef void (*tp_log_callback)(void* context, int level, const char* tag, const char* content);

/** 预加载成功回调 */
typedef void (*tp_preload_success_callback)(void* context, int preload_id);

/** 预加载错误回调 */
typedef void (*tp_preload_error_callback)(void* context, int preload_id, const TPCError* error);

/** 预加载进度回调 */
typedef void (*tp_preload_progress_callback)(void* context, int preload_id, const TPCDownloadProgressInfo* progress);

/* ======================== 播放器生命周期 ======================== */

/**
 * 创建播放器实例
 * @return 播放器句柄，失败返回 NULL
 */
TPPlayerHandle tp_c_player_create(void);

/**
 * 销毁播放器实例并释放资源
 * @param handle 播放器句柄
 */
void tp_c_player_destroy(TPPlayerHandle handle);

/* ======================== 播放控制 ======================== */

int tp_c_player_prepare_async(TPPlayerHandle handle);
int tp_c_player_start(TPPlayerHandle handle);
int tp_c_player_pause(TPPlayerHandle handle);
int tp_c_player_stop(TPPlayerHandle handle);
void tp_c_player_stop_async(TPPlayerHandle handle);
void tp_c_player_reset(TPPlayerHandle handle);
void tp_c_player_release(TPPlayerHandle handle);

/* ======================== 数据设置 ======================== */

int tp_c_player_set_data_source(TPPlayerHandle handle, TPMediaAssetHandle asset);
int tp_c_player_set_video_render_target(TPPlayerHandle handle, const char* surface_id);

/* ======================== 回调注册 ======================== */

void tp_c_player_set_on_prepared_callback(
    TPPlayerHandle handle, tp_on_prepared_callback callback, void* context);

void tp_c_player_set_on_completion_callback(
    TPPlayerHandle handle, tp_on_completion_callback callback, void* context);

void tp_c_player_set_on_error_callback(
    TPPlayerHandle handle, tp_on_error_callback callback, void* context);

void tp_c_player_set_on_info_callback(
    TPPlayerHandle handle, tp_on_info_callback callback, void* context);

void tp_c_player_set_on_seek_complete_callback(
    TPPlayerHandle handle, tp_on_seek_complete_callback callback, void* context);

void tp_c_player_set_on_video_size_changed_callback(
    TPPlayerHandle handle, tp_on_video_size_changed_callback callback, void* context);

void tp_c_player_set_on_state_changed_callback(
    TPPlayerHandle handle, tp_on_state_changed_callback callback, void* context);

void tp_c_player_set_on_stop_async_complete_callback(
    TPPlayerHandle handle, tp_on_stop_async_complete_callback callback, void* context);

void tp_c_player_set_on_media_asset_expire_callback(
    TPPlayerHandle handle, tp_on_media_asset_expire_callback callback, void* context);

/* ======================== 属性查询 ======================== */

int tp_c_player_get_duration_ms(TPPlayerHandle handle);
int tp_c_player_get_current_position_ms(TPPlayerHandle handle);
int64_t tp_c_player_get_available_position_ms(TPPlayerHandle handle);
int tp_c_player_get_width(TPPlayerHandle handle);
int tp_c_player_get_height(TPPlayerHandle handle);
int tp_c_player_get_current_state(TPPlayerHandle handle);

/**
 * 获取播放器属性
 * @param handle 播放器句柄
 * @param property_id 属性 ID 字符串
 * @return 属性值字符串，调用方需使用 tp_c_free_string() 释放
 */
const char* tp_c_player_get_property(TPPlayerHandle handle, const char* property_id);

/* ======================== 播放参数设置 ======================== */

int tp_c_player_seek_to_async(TPPlayerHandle handle, int position_ms, int seek_mode, int64_t opaque);
void tp_c_player_set_audio_mute(TPPlayerHandle handle, int is_mute);
void tp_c_player_set_audio_volume(TPPlayerHandle handle, float volume);
void tp_c_player_set_play_speed_ratio(TPPlayerHandle handle, float speed_ratio);
void tp_c_player_set_video_gravity(TPPlayerHandle handle, int gravity);
void tp_c_player_set_loopback(TPPlayerHandle handle, int is_loopback, int64_t start_pos_ms, int64_t end_pos_ms);
void tp_c_player_set_log_tag_prefix(TPPlayerHandle handle, const char* prefix);

/* ======================== OptionalParam 设置 ======================== */

void tp_c_player_add_optional_param_int(TPPlayerHandle handle, const char* key, int value);
void tp_c_player_add_optional_param_bool(TPPlayerHandle handle, const char* key, int value);
void tp_c_player_add_optional_param_long(TPPlayerHandle handle, const char* key, int64_t value);
void tp_c_player_add_optional_param_float(TPPlayerHandle handle, const char* key, float value);
void tp_c_player_add_optional_param_string(TPPlayerHandle handle, const char* key, const char* value);
void tp_c_player_add_optional_param_queue_int(TPPlayerHandle handle, const char* key, const int* values, int count);

/* ======================== 换源 ======================== */

int tp_c_player_switch_data_source_async(
    TPPlayerHandle handle, TPMediaAssetHandle asset, int mode, int64_t opaque);

/* ======================== MediaAsset 创建与操作 ======================== */

/**
 * 创建 URL 类型的 MediaAsset
 * @param url 视频 URL
 * @return MediaAsset 句柄，失败返回 NULL
 */
TPMediaAssetHandle tp_c_create_url_media_asset(const char* url);

/**
 * 创建 DRM 类型的 MediaAsset
 * @param url 视频 URL
 * @param drm_type DRM 类型
 * @param cert_url 证书 URL
 * @param lic_url 许可证 URL
 * @return MediaAsset 句柄，失败返回 NULL
 */
TPMediaAssetHandle tp_c_create_drm_media_asset(
    const char* url, int drm_type, const char* cert_url, const char* lic_url);

/**
 * 设置 MediaAsset 参数
 * @param asset MediaAsset 句柄
 * @param key 参数键
 * @param value 参数值
 */
void tp_c_asset_set_param(TPMediaAssetHandle asset, const char* key, const char* value);

/**
 * 设置 MediaAsset HTTP 头
 * @param asset MediaAsset 句柄
 * @param keys 键数组
 * @param values 值数组
 * @param count 键值对数量
 */
void tp_c_asset_set_http_header(
    TPMediaAssetHandle asset, const char** keys, const char** values, int count);

/**
 * 添加备用 URL
 * @param asset MediaAsset 句柄
 * @param back_url 备用 URL
 */
void tp_c_asset_add_back_url(TPMediaAssetHandle asset, const char* back_url);

/**
 * 销毁 MediaAsset
 * @param asset MediaAsset 句柄
 */
void tp_c_asset_destroy(TPMediaAssetHandle asset);

/**
 * 通过 MediaAssetRequest 更新 MediaAsset（换源回调中使用）
 * @param request MediaAssetRequest 句柄
 * @param new_asset 新的 MediaAsset 句柄
 */
void tp_c_media_asset_request_update(TPMediaAssetRequestHandle request, TPMediaAssetHandle new_asset);

/* ======================== SDK 管理 ======================== */

/**
 * 初始化 ThumbPlayer SDK
 */
void tp_c_mgr_init(void);

/**
 * 查询 SDK 是否已初始化
 * @return 1=已初始化, 0=未初始化
 */
int tp_c_mgr_is_initialized(void);

/**
 * 获取 ThumbPlayer 版本号
 * @return 版本号字符串，调用方需使用 tp_c_free_string() 释放
 */
const char* tp_c_mgr_get_version(void);

/**
 * 设置日志回调
 * @param callback 日志回调函数指针
 * @param context 用户上下文指针
 */
void tp_c_mgr_set_log_callback(tp_log_callback callback, void* context);

/**
 * 设置 SDK 整数类型可选参数
 * @param key 参数键
 * @param value 参数值
 */
void tp_c_mgr_add_optional_param_int(const char* key, int value);

/**
 * 设置 SDK 字符串类型可选参数
 * @param key 参数键
 * @param value 参数值
 */
void tp_c_mgr_add_optional_param_string(const char* key, const char* value);

/* ======================== 预加载 ======================== */

/**
 * 创建预加载器
 * @return 预加载器句柄，失败返回 NULL
 */
TPPreloaderHandle tp_c_preloader_create(void);

/**
 * 设置预加载回调
 * @param handle 预加载器句柄
 * @param success_cb 成功回调
 * @param error_cb 错误回调
 * @param progress_cb 进度回调
 * @param context 用户上下文指针
 */
void tp_c_preloader_set_callback(
    TPPreloaderHandle handle,
    tp_preload_success_callback success_cb,
    tp_preload_error_callback error_cb,
    tp_preload_progress_callback progress_cb,
    void* context);

/**
 * 开始预加载
 * @param handle 预加载器句柄
 * @param asset MediaAsset 句柄
 * @return 预加载 ID
 */
int tp_c_preloader_start(TPPreloaderHandle handle, TPMediaAssetHandle asset);

/**
 * 停止预加载
 * @param handle 预加载器句柄
 * @param preload_id 预加载 ID
 */
void tp_c_preloader_stop(TPPreloaderHandle handle, int preload_id);

/**
 * 销毁预加载器
 * @param handle 预加载器句柄
 */
void tp_c_preloader_destroy(TPPreloaderHandle handle);

/* ======================== 解码能力查询 ======================== */

/**
 * 查询视频解码能力
 * @param codec_type 编解码类型
 * @param decoder_type 解码器类型
 * @param width 视频宽
 * @param height 视频高
 * @param frame_rate 帧率
 * @return 解码能力值
 */
int tp_c_get_decoder_capability(int codec_type, int decoder_type, int width, int height, int frame_rate);

/* ======================== DataTransport 配置 ======================== */

/**
 * 注册业务 bizId 并关联数据存储目录。
 * 必须在 TPDataTransport_Init 之后、CreateTask 之前调用。
 * @param assigned_biz_id 业务方自定义的 bizId（需 > 10000）
 * @param data_dir 数据存储目录
 * @return 注册成功返回 assigned_biz_id，失败返回 -1
 */
int32_t tp_c_dt_register_assigned_biz_id(int32_t assigned_biz_id, const char* data_dir);

void tp_c_dt_set_global_optional_config_param(const char* key, const char* value);
void tp_c_dt_set_biz_optional_config_param(int biz_id, const char* key, const char* value);

/* ======================== 内存释放工具 ======================== */

/**
 * 释放 C API 返回的字符串
 * @param str 需要释放的字符串指针
 */
void tp_c_free_string(const char* str);

/* ======================== OnInfo 子对象类型常量 ======================== */

#define TPC_OBJ_TYPE_NONE                  0
#define TPC_OBJ_TYPE_DOWNLOAD_PROGRESS     1
#define TPC_OBJ_TYPE_VIDEO_SEI             2

#ifdef __cplusplus
}
#endif

#endif /* THUMBPLAYER_C_API_H */
