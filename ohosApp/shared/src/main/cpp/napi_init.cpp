#include "napi/native_api.h"
#include "libumbrella_api.h"
#include <hilog/log.h>
#include <Kuikly/Kuikly.h>
#include <cstring>

namespace {
constexpr char kIconFontFamily[] = "iconfont";
constexpr char kIconFontRawfileUri[] = "rawfile:iconfont/iconfont_new_style.ttf";

// Kuikly 鸿蒙端 Text 走 OH_Drawing_*（Skia）渲染，字体查询必须通过
// KRRegisterFontAdapter 注入，ArkUI 的 font.registerFont 对其无效。
// 返回 rawfile: URI，Kuikly 底层会自行读取 HAP 内的 rawfile 资源。
char *IconFontAdapter(const char *fontFamily,
                      char ** /*fontBuffer*/,
                      size_t * /*len*/,
                      KRFontDataDeallocator * /*deallocator*/) {
    if (fontFamily != nullptr && strcmp(fontFamily, kIconFontFamily) == 0) {
        return const_cast<char *>(kIconFontRawfileUri);
    }
    return nullptr;
}

bool g_kuiklyInitialized = false;
}  // namespace

static napi_value InitKuikly(napi_env env, napi_callback_info info) {
    // 在 Kuikly 初始化前完成 iconfont 字体 adapter 注册。
    if (!g_kuiklyInitialized) {
        g_kuiklyInitialized = true;
        KRRegisterFontAdapter(&IconFontAdapter, kIconFontFamily);
    }

    auto api = libumbrella_symbols();
    int handler = api->kotlin.root.initKuikly();
    napi_value result;
    napi_create_int32(env, handler, &result);
    return result;
}

EXTERN_C_START
static napi_value Init(napi_env env, napi_value exports)
{
    napi_property_descriptor desc[] = {
        {"initKuikly", nullptr, InitKuikly, nullptr, nullptr, nullptr, napi_default, nullptr},
    };
    napi_define_properties(env, exports, sizeof(desc) / sizeof(desc[0]), desc);
    return exports;
}
EXTERN_C_END

static napi_module demoModule = {
    .nm_version = 1,
    .nm_flags = 0,
    .nm_filename = nullptr,
    .nm_register_func = Init,
    .nm_modname = "kmm_shared",
    .nm_priv = ((void*)0),
    .reserved = { 0 },
};

extern "C" __attribute__((constructor)) void RegisterKmmSharedModule(void)
{
    napi_module_register(&demoModule);
}
