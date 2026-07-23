/**
 * Copyright (c) 2024 Tencent. All rights reserved.
 *
 * ThumbPlayer C API 桥接层实现
 * 将 ThumbPlayer C++ 核心接口扁平化为纯 C 接口，
 * 供 Kotlin/Native cinterop 工具生成 Kotlin 绑定。
 *
 * @author codebuddy
 * @date 2026/03/19
 */

#include "thumbplayer_c_api.h"

#include <cstdlib>
#include <cstring>
#include <map>
#include <memory>
#include <string>

#include "api/player/tp_player_factory.h"
#include "api/player/tp_player_interface.h"
#include "api/player/tp_player_callback.h"
#include "api/player/tp_media_asset_request_interface.h"
#include "api/preload/tp_preloader_interface.h"
#include "api/common/tp_download_progress_info.h"
#include "api/manager/tp_mgr.h"
#include "utils2/asset/tp2_core_url_media_asset.h"
#include "utils2/asset/tp2_core_drm_media_asset.h"
#include "utils2/tp2_video_render_target_factory.h"
#include "utils2/string/tp2_string_util.h"
#include "modules2/decoder/capability/harmony/tp2_harmony_decoder_capability.h"

// DataTransport C++ API（离线下载配置）
#include "itp_data_transport_api.h"

using namespace thumbplayer;

/* ======================== 内部回调适配器 ======================== */

/**
 * C 层回调适配器，继承 C++ 回调接口，将 C++ 回调转发给 C 函数指针
 */
class TPCCallbackAdapter : public IOnPreparedCallback,
                           public IOnCompletionCallback,
                           public IOnInfoCallback,
                           public IOnErrorCallback,
                           public IOnSeekCompleteCallback,
                           public IOnVideoSizeChangedCallback,
                           public IOnStateChangedCallback,
                           public IOnStopAsyncCompleteCallback,
                           public IOnMediaAssetExpireCallback,
                           public std::enable_shared_from_this<TPCCallbackAdapter> {
public:
    // C 函数指针及上下文
    tp_on_prepared_callback on_prepared_cb = nullptr;
    void* on_prepared_ctx = nullptr;

    tp_on_completion_callback on_completion_cb = nullptr;
    void* on_completion_ctx = nullptr;

    tp_on_error_callback on_error_cb = nullptr;
    void* on_error_ctx = nullptr;

    tp_on_info_callback on_info_cb = nullptr;
    void* on_info_ctx = nullptr;

    tp_on_seek_complete_callback on_seek_complete_cb = nullptr;
    void* on_seek_complete_ctx = nullptr;

    tp_on_video_size_changed_callback on_video_size_changed_cb = nullptr;
    void* on_video_size_changed_ctx = nullptr;

    tp_on_state_changed_callback on_state_changed_cb = nullptr;
    void* on_state_changed_ctx = nullptr;

    tp_on_stop_async_complete_callback on_stop_async_complete_cb = nullptr;
    void* on_stop_async_complete_ctx = nullptr;

    tp_on_media_asset_expire_callback on_media_asset_expire_cb = nullptr;
    void* on_media_asset_expire_ctx = nullptr;

    // C++ 回调接口实现
    void OnPrepared(ITPPlayer* player) override {
        if (on_prepared_cb) {
            on_prepared_cb(on_prepared_ctx);
        }
    }

    void OnCompletion(ITPPlayer* player) override {
        if (on_completion_cb) {
            on_completion_cb(on_completion_ctx);
        }
    }

    void OnError(ITPPlayer* player, const tp2_utils::TPError& error) override {
        if (on_error_cb) {
            TPCError c_error;
            c_error.error_type = static_cast<int>(error.error_type);
            c_error.error_code = static_cast<int>(error.error_code);
            c_error.current_pos_ms = error.current_position_ms;
            on_error_cb(on_error_ctx, &c_error);
        }
    }

    void OnInfo(ITPPlayer* player, tp2_framework::player::TPOnInfoID on_info_id,
                std::shared_ptr<tp2_framework::player::TPCoreOnInfoParam> on_info_param) override {
        if (on_info_cb) {
            TPCOnInfoParam c_param = {};

            if (on_info_param) {
                c_param.long_param1 = on_info_param->long_param1;
                c_param.long_param2 = on_info_param->long_param2;
                c_param.float_param1 = on_info_param->float_param1;
                c_param.float_param2 = on_info_param->float_param2;

                auto str1 = on_info_param->str_param1;
                auto str2 = on_info_param->str_param2;
                c_param.str_param1 = str1.c_str();
                c_param.str_param2 = str2.c_str();
                c_param.obj_type = TPC_OBJ_TYPE_NONE;
                c_param.obj_param = nullptr;

                // 注意：TPCoreOnInfoParam 是抽象接口，当前 stub 头文件不包含
                // GetDownloadProgressInfo() 和 GetVideoSeiInfo() 方法。
                // 如果后续 SDK 头文件提供了这些方法，可在此处扩展解析。
            }

            on_info_cb(on_info_ctx, static_cast<int>(on_info_id), &c_param);
        }
    }

    void OnSeekComplete(ITPPlayer* player, int64_t opaque) override {
        if (on_seek_complete_cb) {
            on_seek_complete_cb(on_seek_complete_ctx, opaque);
        }
    }

    void OnVideoSizeChanged(ITPPlayer* player, int width, int height) override {
        if (on_video_size_changed_cb) {
            on_video_size_changed_cb(on_video_size_changed_ctx, width, height);
        }
    }

    void OnStateChanged(ITPPlayer* player, TPPlayerState pre_state, TPPlayerState cur_state) override {
        if (on_state_changed_cb) {
            on_state_changed_cb(on_state_changed_ctx,
                                static_cast<int>(pre_state),
                                static_cast<int>(cur_state));
        }
    }

    void OnStopAsyncComplete(ITPPlayer* player) override {
        if (on_stop_async_complete_cb) {
            on_stop_async_complete_cb(on_stop_async_complete_ctx);
        }
    }

    void OnMediaAssetExpire(ITPPlayer* player, ITPMediaAssetRequest* request) override {
        if (on_media_asset_expire_cb) {
            on_media_asset_expire_cb(on_media_asset_expire_ctx, static_cast<TPMediaAssetRequestHandle>(request));
        }
    }
};

/**
 * 预加载回调适配器（使用嵌套类 ITPPreloader::ITPPreloadCallback）
 */
class TPCPreloadCallbackAdapter : public ITPPreloader::ITPPreloadCallback {
public:
    tp_preload_success_callback success_cb = nullptr;
    tp_preload_error_callback error_cb = nullptr;
    tp_preload_progress_callback progress_cb = nullptr;
    void* context = nullptr;

    void OnPreloadSuccess(int32_t preload_id) override {
        if (success_cb) {
            success_cb(context, preload_id);
        }
    }

    void OnPreloadError(int32_t preload_id, const tp2_utils::TPError& error) override {
        if (error_cb) {
            TPCError c_error;
            c_error.error_type = static_cast<int>(error.error_type);
            c_error.error_code = static_cast<int>(error.error_code);
            c_error.current_pos_ms = error.current_position_ms;
            error_cb(context, preload_id, &c_error);
        }
    }

    void OnPreloadProgressUpdate(int32_t preload_id, const TPDownloadProgressInfo& progress) override {
        if (progress_cb) {
            TPCDownloadProgressInfo c_progress;
            c_progress.available_position_ms = static_cast<int>(progress.available_position_ms);
            c_progress.download_speed_bps = static_cast<int>(progress.download_speed_bps);
            c_progress.download_bytes = static_cast<int>(progress.download_bytes);
            c_progress.file_total_bytes = static_cast<int>(progress.file_total_bytes);
            c_progress.extra_info = progress.extra_info.c_str();
            progress_cb(context, preload_id, &c_progress);
        }
    }
};

/* ======================== 内部辅助结构 ======================== */

/** 播放器内部状态，封装 C++ 对象 */
struct TPCPlayerInternal {
    std::unique_ptr<ITPPlayer> player;
    std::shared_ptr<TPCCallbackAdapter> callback;
};

/** 日志回调状态 */
static tp_log_callback g_log_callback = nullptr;
static void* g_log_context = nullptr;

/* ======================== 播放器生命周期 ======================== */

TPPlayerHandle tp_c_player_create(void) {
    auto internal = new (std::nothrow) TPCPlayerInternal();
    if (!internal) return nullptr;

    internal->player = TPPlayerFactory::CreateTPPlayer(nullptr);
    if (!internal->player) {
        delete internal;
        return nullptr;
    }

    internal->callback = std::make_shared<TPCCallbackAdapter>();
    return static_cast<TPPlayerHandle>(internal);
}

void tp_c_player_destroy(TPPlayerHandle handle) {
    if (!handle) return;
    auto internal = static_cast<TPCPlayerInternal*>(handle);
    delete internal;
}

/* ======================== 内部辅助宏 ======================== */

#define GET_INTERNAL(handle) static_cast<TPCPlayerInternal*>(handle)
#define GET_PLAYER(handle) (GET_INTERNAL(handle)->player.get())

/* ======================== 播放控制 ======================== */

int tp_c_player_prepare_async(TPPlayerHandle handle) {
    if (!handle) return -1;
    return GET_PLAYER(handle)->PrepareAsync();
}

int tp_c_player_start(TPPlayerHandle handle) {
    if (!handle) return -1;
    return GET_PLAYER(handle)->Start();
}

int tp_c_player_pause(TPPlayerHandle handle) {
    if (!handle) return -1;
    return GET_PLAYER(handle)->Pause();
}

int tp_c_player_stop(TPPlayerHandle handle) {
    if (!handle) return -1;
    return GET_PLAYER(handle)->Stop();
}

void tp_c_player_stop_async(TPPlayerHandle handle) {
    if (!handle) return;
    GET_PLAYER(handle)->StopAsync();
}

void tp_c_player_reset(TPPlayerHandle handle) {
    if (!handle) return;
    GET_PLAYER(handle)->Reset();
}

void tp_c_player_release(TPPlayerHandle handle) {
    if (!handle) return;
    GET_PLAYER(handle)->Release();
}

/* ======================== 数据设置 ======================== */

int tp_c_player_set_data_source(TPPlayerHandle handle, TPMediaAssetHandle asset) {
    if (!handle || !asset) return -1;
    auto media_asset = static_cast<tp2_utils::asset::TPCoreMediaAsset*>(asset);
    // 使用非拥有删除器包装为 shared_ptr，避免双重释放
    return GET_PLAYER(handle)->SetDataSource(
        std::shared_ptr<tp2_utils::asset::TPCoreMediaAsset>(media_asset, [](tp2_utils::asset::TPCoreMediaAsset*) {}));
}

int tp_c_player_set_video_render_target(TPPlayerHandle handle, const char* surface_id) {
    if (!handle) return -1;
    if (!surface_id) {
        return GET_PLAYER(handle)->SetVideoRenderTarget(nullptr);
    }
    uint64_t tmp_surface_id = 0;
    if (!tp2_utils::string::TPStringUtil::ToUInt64(std::string(surface_id), tmp_surface_id)) {
        return -1;
    }
    auto render_target = tp2_utils::TPVideoRenderTargetFactory::Create(&tmp_surface_id);
    return GET_PLAYER(handle)->SetVideoRenderTarget(render_target);
}

/* ======================== 回调注册 ======================== */

void tp_c_player_set_on_prepared_callback(
    TPPlayerHandle handle, tp_on_prepared_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_prepared_cb = callback;
    internal->callback->on_prepared_ctx = context;
    if (callback) {
        std::weak_ptr<IOnPreparedCallback> weak_cb = internal->callback;
        internal->player->SetOnPreparedCallback(weak_cb);
    } else {
        std::weak_ptr<IOnPreparedCallback> empty;
        internal->player->SetOnPreparedCallback(empty);
    }
}

void tp_c_player_set_on_completion_callback(
    TPPlayerHandle handle, tp_on_completion_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_completion_cb = callback;
    internal->callback->on_completion_ctx = context;
    if (callback) {
        std::weak_ptr<IOnCompletionCallback> weak_cb = internal->callback;
        internal->player->SetOnCompletionCallback(weak_cb);
    } else {
        std::weak_ptr<IOnCompletionCallback> empty;
        internal->player->SetOnCompletionCallback(empty);
    }
}

void tp_c_player_set_on_error_callback(
    TPPlayerHandle handle, tp_on_error_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_error_cb = callback;
    internal->callback->on_error_ctx = context;
    if (callback) {
        std::weak_ptr<IOnErrorCallback> weak_cb = internal->callback;
        internal->player->SetOnErrorCallback(weak_cb);
    } else {
        std::weak_ptr<IOnErrorCallback> empty;
        internal->player->SetOnErrorCallback(empty);
    }
}

void tp_c_player_set_on_info_callback(
    TPPlayerHandle handle, tp_on_info_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_info_cb = callback;
    internal->callback->on_info_ctx = context;
    if (callback) {
        std::weak_ptr<IOnInfoCallback> weak_cb = internal->callback;
        internal->player->SetOnInfoCallback(weak_cb);
    } else {
        std::weak_ptr<IOnInfoCallback> empty;
        internal->player->SetOnInfoCallback(empty);
    }
}

void tp_c_player_set_on_seek_complete_callback(
    TPPlayerHandle handle, tp_on_seek_complete_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_seek_complete_cb = callback;
    internal->callback->on_seek_complete_ctx = context;
    if (callback) {
        std::weak_ptr<IOnSeekCompleteCallback> weak_cb = internal->callback;
        internal->player->SetOnSeekCompleteCallback(weak_cb);
    } else {
        std::weak_ptr<IOnSeekCompleteCallback> empty;
        internal->player->SetOnSeekCompleteCallback(empty);
    }
}

void tp_c_player_set_on_video_size_changed_callback(
    TPPlayerHandle handle, tp_on_video_size_changed_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_video_size_changed_cb = callback;
    internal->callback->on_video_size_changed_ctx = context;
    if (callback) {
        std::weak_ptr<IOnVideoSizeChangedCallback> weak_cb = internal->callback;
        internal->player->SetOnVideoSizeChangedCallback(weak_cb);
    } else {
        std::weak_ptr<IOnVideoSizeChangedCallback> empty;
        internal->player->SetOnVideoSizeChangedCallback(empty);
    }
}

void tp_c_player_set_on_state_changed_callback(
    TPPlayerHandle handle, tp_on_state_changed_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_state_changed_cb = callback;
    internal->callback->on_state_changed_ctx = context;
    if (callback) {
        std::weak_ptr<IOnStateChangedCallback> weak_cb = internal->callback;
        internal->player->SetOnStateChangedCallback(weak_cb);
    } else {
        std::weak_ptr<IOnStateChangedCallback> empty;
        internal->player->SetOnStateChangedCallback(empty);
    }
}

void tp_c_player_set_on_stop_async_complete_callback(
    TPPlayerHandle handle, tp_on_stop_async_complete_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_stop_async_complete_cb = callback;
    internal->callback->on_stop_async_complete_ctx = context;
    if (callback) {
        std::weak_ptr<IOnStopAsyncCompleteCallback> weak_cb = internal->callback;
        internal->player->SetOnStopAsyncCompleteCallback(weak_cb);
    } else {
        std::weak_ptr<IOnStopAsyncCompleteCallback> empty;
        internal->player->SetOnStopAsyncCompleteCallback(empty);
    }
}

void tp_c_player_set_on_media_asset_expire_callback(
    TPPlayerHandle handle, tp_on_media_asset_expire_callback callback, void* context) {
    if (!handle) return;
    auto internal = GET_INTERNAL(handle);
    internal->callback->on_media_asset_expire_cb = callback;
    internal->callback->on_media_asset_expire_ctx = context;
    if (callback) {
        std::weak_ptr<IOnMediaAssetExpireCallback> weak_cb = internal->callback;
        internal->player->SetOnMediaAssetExpireCallback(weak_cb);
    } else {
        std::weak_ptr<IOnMediaAssetExpireCallback> empty;
        internal->player->SetOnMediaAssetExpireCallback(empty);
    }
}

/* ======================== 属性查询 ======================== */

int tp_c_player_get_duration_ms(TPPlayerHandle handle) {
    if (!handle) return -1;
    return static_cast<int>(GET_PLAYER(handle)->GetDurationMs());
}

int tp_c_player_get_current_position_ms(TPPlayerHandle handle) {
    if (!handle) return -1;
    return static_cast<int>(GET_PLAYER(handle)->GetCurrentPositionMs());
}

int64_t tp_c_player_get_available_position_ms(TPPlayerHandle handle) {
    if (!handle) return -1;
    return GET_PLAYER(handle)->GetAvailablePositionMs();
}

int tp_c_player_get_width(TPPlayerHandle handle) {
    if (!handle) return 0;
    return GET_PLAYER(handle)->GetWidth();
}

int tp_c_player_get_height(TPPlayerHandle handle) {
    if (!handle) return 0;
    return GET_PLAYER(handle)->GetHeight();
}

int tp_c_player_get_current_state(TPPlayerHandle handle) {
    if (!handle) return 0;
    return static_cast<int>(GET_PLAYER(handle)->GetCurrentState());
}

const char* tp_c_player_get_property(TPPlayerHandle handle, const char* property_id) {
    if (!handle || !property_id) return nullptr;
    std::string result = GET_PLAYER(handle)->GetProperty(std::string(property_id));
    char* c_str = static_cast<char*>(malloc(result.size() + 1));
    if (c_str) {
        memcpy(c_str, result.c_str(), result.size() + 1);
    }
    return c_str;
}

/* ======================== 播放参数设置 ======================== */

int tp_c_player_seek_to_async(TPPlayerHandle handle, int position_ms, int seek_mode, int64_t opaque) {
    if (!handle) return -1;
    return GET_PLAYER(handle)->SeekToAsync(
        static_cast<int64_t>(position_ms),
        static_cast<tp2_utils::TPSeekMode>(seek_mode),
        opaque);
}

void tp_c_player_set_audio_mute(TPPlayerHandle handle, int is_mute) {
    if (!handle) return;
    GET_PLAYER(handle)->SetAudioMute(is_mute != 0);
}

void tp_c_player_set_audio_volume(TPPlayerHandle handle, float volume) {
    if (!handle) return;
    GET_PLAYER(handle)->SetAudioVolume(volume);
}

void tp_c_player_set_play_speed_ratio(TPPlayerHandle handle, float speed_ratio) {
    if (!handle) return;
    GET_PLAYER(handle)->SetPlaySpeedRatio(speed_ratio);
}

void tp_c_player_set_video_gravity(TPPlayerHandle handle, int gravity) {
    if (!handle) return;
    GET_PLAYER(handle)->SetVideoGravity(static_cast<tp2_utils::TPVideoGravity>(gravity));
}

void tp_c_player_set_loopback(TPPlayerHandle handle, int is_loopback, int64_t start_pos_ms, int64_t end_pos_ms) {
    if (!handle) return;
    GET_PLAYER(handle)->SetLoopback(is_loopback != 0, start_pos_ms, end_pos_ms);
}

void tp_c_player_set_log_tag_prefix(TPPlayerHandle handle, const char* prefix) {
    if (!handle || !prefix) return;
    GET_PLAYER(handle)->SetLogTagPrefix(std::string(prefix));
}

/* ======================== OptionalParam 设置 ======================== */

void tp_c_player_add_optional_param_int(TPPlayerHandle handle, const char* key, int value) {
    if (!handle || !key) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    param.SetValue(tp2_utils::TPUniversalType::CreateWithInt32(static_cast<int32_t>(value)));
    GET_PLAYER(handle)->AddOptionalParam(param);
}

void tp_c_player_add_optional_param_bool(TPPlayerHandle handle, const char* key, int value) {
    if (!handle || !key) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    param.SetValue(tp2_utils::TPUniversalType::CreateWithBool(value != 0));
    GET_PLAYER(handle)->AddOptionalParam(param);
}

void tp_c_player_add_optional_param_long(TPPlayerHandle handle, const char* key, int64_t value) {
    if (!handle || !key) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    param.SetValue(tp2_utils::TPUniversalType::CreateWithInt64(value));
    GET_PLAYER(handle)->AddOptionalParam(param);
}

void tp_c_player_add_optional_param_float(TPPlayerHandle handle, const char* key, float value) {
    if (!handle || !key) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    param.SetValue(tp2_utils::TPUniversalType::CreateWithFloat(value));
    GET_PLAYER(handle)->AddOptionalParam(param);
}

void tp_c_player_add_optional_param_string(TPPlayerHandle handle, const char* key, const char* value) {
    if (!handle || !key || !value) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    param.SetValue(tp2_utils::TPUniversalType::CreateWithString(std::string(value)));
    GET_PLAYER(handle)->AddOptionalParam(param);
}

void tp_c_player_add_optional_param_queue_int(TPPlayerHandle handle, const char* key, const int* values, int count) {
    if (!handle || !key || !values || count <= 0) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    std::deque<int32_t> queue;
    for (int i = 0; i < count; ++i) {
        queue.push_back(static_cast<int32_t>(values[i]));
    }
    param.SetValue(tp2_utils::TPUniversalType::CreateWithInt32Queue(queue));
    GET_PLAYER(handle)->AddOptionalParam(param);
}

/* ======================== 换源 ======================== */

int tp_c_player_switch_data_source_async(
    TPPlayerHandle handle, TPMediaAssetHandle asset, int mode, int64_t opaque) {
    if (!handle || !asset) return -1;
    auto media_asset = static_cast<tp2_utils::asset::TPCoreMediaAsset*>(asset);
    return GET_PLAYER(handle)->SwitchDataSourceAsync(
        std::shared_ptr<tp2_utils::asset::TPCoreMediaAsset>(media_asset, [](tp2_utils::asset::TPCoreMediaAsset*) {}),
        static_cast<tp2_utils::TPSwitchDataSourceMode>(mode),
        opaque);
}

/* ======================== MediaAsset 创建与操作 ======================== */

TPMediaAssetHandle tp_c_create_url_media_asset(const char* url) {
    if (!url) return nullptr;
    auto asset = new (std::nothrow) tp2_utils::asset::TPCoreUrlMediaAsset();
    if (!asset) return nullptr;
    asset->SetUrl(std::string(url));
    return static_cast<TPMediaAssetHandle>(asset);
}

TPMediaAssetHandle tp_c_create_drm_media_asset(
    const char* url, int drm_type, const char* cert_url, const char* lic_url) {
    if (!url || !cert_url || !lic_url) return nullptr;
    auto asset = new (std::nothrow) tp2_utils::asset::TPCoreDrmMediaAsset();
    if (!asset) return nullptr;
    asset->SetUrl(std::string(url));
    asset->SetDrmType(static_cast<tp2_modules::drm::TPDrmSchemeType>(drm_type));
    asset->SetCertificateUrl(std::string(cert_url), {});
    asset->SetLicenseUrl(std::string(lic_url), {});
    return static_cast<TPMediaAssetHandle>(asset);
}

void tp_c_asset_set_param(TPMediaAssetHandle asset, const char* key, const char* value) {
    if (!asset || !key || !value) return;
    auto media_asset = static_cast<tp2_utils::asset::TPCoreMediaAsset*>(asset);
    media_asset->SetParam(std::string(key), std::string(value));
}

void tp_c_asset_set_http_header(
    TPMediaAssetHandle asset, const char** keys, const char** values, int count) {
    if (!asset || !keys || !values || count <= 0) return;
    auto url_asset = dynamic_cast<tp2_utils::asset::TPCoreUrlMediaAsset*>(
        static_cast<tp2_utils::asset::TPCoreMediaAsset*>(asset));
    if (!url_asset) return;
    std::map<std::string, std::string> headers;
    for (int i = 0; i < count; ++i) {
        if (keys[i] && values[i]) {
            headers[std::string(keys[i])] = std::string(values[i]);
        }
    }
    url_asset->SetHttpHeader(headers);
}

void tp_c_asset_add_back_url(TPMediaAssetHandle asset, const char* back_url) {
    if (!asset || !back_url) return;
    auto url_asset = dynamic_cast<tp2_utils::asset::TPCoreUrlMediaAsset*>(
        static_cast<tp2_utils::asset::TPCoreMediaAsset*>(asset));
    if (!url_asset) return;
    url_asset->AddBackUrl(std::string(back_url));
}

void tp_c_asset_destroy(TPMediaAssetHandle asset) {
    if (!asset) return;
    auto media_asset = static_cast<tp2_utils::asset::TPCoreMediaAsset*>(asset);
    delete media_asset;
}

void tp_c_media_asset_request_update(TPMediaAssetRequestHandle request, TPMediaAssetHandle new_asset) {
    if (!request || !new_asset) return;
    auto req = static_cast<ITPMediaAssetRequest*>(request);
    auto media_asset = static_cast<tp2_utils::asset::TPCoreMediaAsset*>(new_asset);
    req->UpdateMediaAsset(
        std::shared_ptr<tp2_utils::asset::TPCoreMediaAsset>(media_asset, [](tp2_utils::asset::TPCoreMediaAsset*) {}));
}

/* ======================== SDK 管理 ======================== */

void tp_c_mgr_init(void) {
    TPMgr::GetInstance()->InitThumbPlayer(nullptr);
}

int tp_c_mgr_is_initialized(void) {
    return TPMgr::GetInstance()->IsInitialized() ? 1 : 0;
}

const char* tp_c_mgr_get_version(void) {
    std::string version = TPMgr::GetInstance()->GetThumbPlayerVersion();
    char* c_str = static_cast<char*>(malloc(version.size() + 1));
    if (c_str) {
        memcpy(c_str, version.c_str(), version.size() + 1);
    }
    return c_str;
}

void tp_c_mgr_set_log_callback(tp_log_callback callback, void* context) {
    g_log_callback = callback;
    g_log_context = context;
    if (callback) {
        // TPLogCallback 签名: void (*)(TPLogLevel, const char*, const char*)
        TPMgr::GetInstance()->SetLogCallback(
            [](TPLogLevel level, const char* tag, const char* content) {
                if (g_log_callback) {
                    g_log_callback(g_log_context, static_cast<int>(level), tag, content);
                }
            });
    } else {
        TPMgr::GetInstance()->SetLogCallback(nullptr);
    }
}

void tp_c_mgr_add_optional_param_int(const char* key, int value) {
    if (!key) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    param.SetValue(tp2_utils::TPUniversalType::CreateWithInt32(static_cast<int32_t>(value)));
    TPMgr::GetInstance()->AddOptionalParam(param);
}

void tp_c_mgr_add_optional_param_string(const char* key, const char* value) {
    if (!key || !value) return;
    tp2_utils::TPCoreOptionalParam param;
    param.SetKey(std::string(key));
    param.SetValue(tp2_utils::TPUniversalType::CreateWithString(std::string(value)));
    TPMgr::GetInstance()->AddOptionalParam(param);
}

/* ======================== 预加载 ======================== */

struct TPCPreloaderInternal {
    std::unique_ptr<ITPPreloader> preloader;
    std::shared_ptr<TPCPreloadCallbackAdapter> callback;
};

TPPreloaderHandle tp_c_preloader_create(void) {
    auto internal = new (std::nothrow) TPCPreloaderInternal();
    if (!internal) return nullptr;

    internal->preloader = ITPPreloader::Create();
    if (!internal->preloader) {
        delete internal;
        return nullptr;
    }

    internal->callback = std::make_shared<TPCPreloadCallbackAdapter>();
    return static_cast<TPPreloaderHandle>(internal);
}

void tp_c_preloader_set_callback(
    TPPreloaderHandle handle,
    tp_preload_success_callback success_cb,
    tp_preload_error_callback error_cb,
    tp_preload_progress_callback progress_cb,
    void* context) {
    if (!handle) return;
    auto internal = static_cast<TPCPreloaderInternal*>(handle);
    internal->callback->success_cb = success_cb;
    internal->callback->error_cb = error_cb;
    internal->callback->progress_cb = progress_cb;
    internal->callback->context = context;

    std::weak_ptr<ITPPreloader::ITPPreloadCallback> weak_cb = internal->callback;
    internal->preloader->SetPreloadCallback(weak_cb);
}

int tp_c_preloader_start(TPPreloaderHandle handle, TPMediaAssetHandle asset) {
    if (!handle || !asset) return -1;
    auto internal = static_cast<TPCPreloaderInternal*>(handle);
    auto media_asset = static_cast<tp2_utils::asset::TPCoreMediaAsset*>(asset);
    return internal->preloader->Start(
        std::shared_ptr<tp2_utils::asset::TPCoreMediaAsset>(media_asset, [](tp2_utils::asset::TPCoreMediaAsset*) {}));
}

void tp_c_preloader_stop(TPPreloaderHandle handle, int preload_id) {
    if (!handle) return;
    auto internal = static_cast<TPCPreloaderInternal*>(handle);
    internal->preloader->Stop(preload_id);
}

void tp_c_preloader_destroy(TPPreloaderHandle handle) {
    if (!handle) return;
    auto internal = static_cast<TPCPreloaderInternal*>(handle);
    delete internal;
}


/* ======================== 解码能力查询 ======================== */

int tp_c_get_decoder_capability(int codec_type, int decoder_type, int width, int height, int frame_rate) {
  // TPHarmonyDecoderCapability::GetVideoDecoderCapability 接受 TPCodecID
  // codec_type 对应 TPCodecID，decoder_type 在此 API 中暂未使用
  return static_cast<int>(
      tp2_modules::decoder::TPHarmonyDecoderCapability::GetInstance().GetVideoDecoderCapability(
          static_cast<tp2_utils::TPCodecID>(codec_type),
          width, height, static_cast<float>(frame_rate)));
}

/* ======================== DataTransport 配置 ======================== */

int32_t tp_c_dt_register_assigned_biz_id(int32_t assigned_biz_id, const char* data_dir) {
  auto* factory = GetTPDataTransportFactory();
  if (!factory) return -1;
  auto* mgr = factory->GetDataTransportMgr();
  if (!mgr) return -1;
  return mgr->RegisterAssignedBizId(assigned_biz_id, data_dir ? data_dir : "");
}

void tp_c_dt_set_global_optional_config_param(const char* key, const char* value) {
  if (!key || !value) return;
  auto* factory = GetTPDataTransportFactory();
  if (!factory) return;
  auto* mgr = factory->GetDataTransportMgr();
  if (!mgr) return;
  mgr->SetGlobalOptionalConfigParam(key, value);
}

void tp_c_dt_set_biz_optional_config_param(int biz_id, const char* key, const char* value) {
  if (!key || !value) return;
  auto* factory = GetTPDataTransportFactory();
  if (!factory) return;
  auto* mgr = factory->GetDataTransportMgr();
  if (!mgr) return;
  mgr->SetBizOptionalConfigParam(static_cast<int32_t>(biz_id), key, value);
}


/* ======================== 内存释放工具 ======================== */

void tp_c_free_string(const char* str) {
    if (str) {
        free(const_cast<char*>(str));
    }
}
