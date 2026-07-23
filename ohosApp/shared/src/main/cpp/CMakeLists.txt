# the minimum version of CMake.
cmake_minimum_required(VERSION 3.4.1)
project(KmmShared)

set(NATIVERENDER_ROOT_PATH ${CMAKE_CURRENT_SOURCE_DIR})

if (DEFINED PACKAGE_FIND_FILE)
    include(${PACKAGE_FIND_FILE})
endif ()

include_directories(${NATIVERENDER_ROOT_PATH}
        ${NATIVERENDER_ROOT_PATH}/include
        ${NATIVERENDER_ROOT_PATH}/include/third_party)

# thumbplayer 动态库
add_library(thumbplayer SHARED IMPORTED)
set_target_properties(thumbplayer
        PROPERTIES
        IMPORTED_LOCATION ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/thumbplayer/libs/${OHOS_ARCH}/libThumbPlayer.so)

# thumbplayer C Wrapper - 为KN工程提供C接口包装
add_library(thumbplayer_c_wrapper SHARED
        include/third_part/thumbplayer_wrapper/thumbplayer_c_api.cpp)

# 设置头文件包含路径
target_include_directories(thumbplayer_c_wrapper PUBLIC
        ${NATIVERENDER_ROOT_PATH}/include/third_part/thumbplayer_wrapper
        ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/thumbplayer/include/headers
        ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/tpdatatransport/include)

# 链接thumbplayer静态库及其依赖的动态库、鸿蒙系统库到thumbplayer_c_wrapper
# 必须使用 --whole-archive 包裹 thumbplayer 静态库，
# 因为解码能力模型通过 C++ 全局构造函数自注册（TP_REGISTER_VIDEO_DECODER_MODEL），
# 若不强制包含所有 .o，链接器会将这些"无引用"的注册代码剥离，导致解码能力查询失败、视频黑屏。
target_link_libraries(thumbplayer_c_wrapper PUBLIC
        -Wl,--whole-archive
        thumbplayer
        -Wl,--no-whole-archive
        tpdatatransport
        libz.so
        libEGL.so
        libGLESv3.so
        libOpenSLES.so
        # 鸿蒙系统 NDK 库 (ThumbPlayer 静态库依赖)
        libavplayer.so
        libnative_media_codecbase.so
        libnative_media_core.so
        libnative_media_acodec.so
        libnative_media_vdec.so
        libace_napi.z.so
        libohaudio.so
        libdeviceinfo_ndk.z.so
        libnative_drawing.so
        libnative_window.so
        libace_ndk.z.so
        libimage_source.so
        libpixelmap.so
        libhilog_ndk.z.so)

# tpdatatransport 动态库（鸿蒙离线下载）
add_library(tpdatatransport SHARED IMPORTED)
set_target_properties(tpdatatransport
        PROPERTIES
        IMPORTED_LOCATION ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/tpdatatransport/libs/${OHOS_ARCH}/libDownloadProxy.so)

# MMKV 动态库
add_library(mmkv SHARED IMPORTED)
set_target_properties(mmkv
        PROPERTIES
        IMPORTED_LOCATION ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/@tencent/mmkv/libs/${OHOS_ARCH}/libmmkv.so)

# MMKV 头文件路径
set(MMKV_INCLUDE_DIR ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/@tencent/mmkv/include)

# MMKV C Wrapper - 为 KN 工程提供 C 接口包装
add_library(mmkv_c_wrapper SHARED
        include/third_party/mmkv/mmkv_c_wrapper.cpp
        include/third_party/mmkv/mmkv_c_wrapper.h)

# 设置 mmkv_c_wrapper 的头文件包含路径
target_include_directories(mmkv_c_wrapper PUBLIC
        ${NATIVERENDER_ROOT_PATH}/include/third_party/mmkv
        ${MMKV_INCLUDE_DIR})

# 链接 mmkv 库到 mmkv_c_wrapper
target_link_libraries(mmkv_c_wrapper PUBLIC mmkv)

add_library(kuikly_render SHARED IMPORTED)
set_target_properties(kuikly_render
        PROPERTIES
        IMPORTED_LOCATION ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/@kuikly/render/libs/${OHOS_ARCH}/libkuikly.so)

# kuikly 头文件路径（用于 napi_init.cpp 引入 Kuikly/Kuikly.h 调用 KRRegisterFontAdapter）
set(KUIKLY_INCLUDE_DIR ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/@kuikly/render/include)

add_library(kuikly_umbrella SHARED IMPORTED)
set_target_properties(kuikly_umbrella
        PROPERTIES
        IMPORTED_LOCATION ${NATIVERENDER_ROOT_PATH}/../../../libs/${OHOS_ARCH}/libumbrella.so)

# 使用Bugly Log输出日志到文件
add_library(bugly_shared_xlog SHARED IMPORTED)
set_target_properties(bugly_shared_xlog
        PROPERTIES
        IMPORTED_LOCATION ${NATIVERENDER_ROOT_PATH}/../../../oh_modules/bugly/libs/${OHOS_ARCH}/libbuglyxlog.so)


add_library(kmm_shared SHARED napi_init.cpp)
target_include_directories(kmm_shared PUBLIC
        ${KUIKLY_INCLUDE_DIR})
target_link_libraries(kmm_shared PUBLIC
        libace_napi.z.so
        libhilog_ndk.z.so
        libnative_display_manager.so
        libnative_window_manager.so
        libohcrypto.so
        libability_runtime.so
        libohvibrator.z.so
        bugly_shared_xlog
        thumbplayer_c_wrapper
        tpdatatransport
        mmkv
        mmkv_c_wrapper
        kuikly_umbrella
        kuikly_render)
