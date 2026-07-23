#!/bin/bash
set -e

# 仅用来检查鸿蒙编译 so 是否成功，编译出的 so 和 har 均为 debug 版本
export QN_COMPAT_BUILD_TYPE=ohos
export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}
export PATH=$JAVA_HOME/bin:$PATH

./gradlew :startup:checkSchemeDoc :umbrella:linkDebugSharedOhosArm64 -Pqqnews.kmm.build.platform=ohos -Pdebug.mode=true

SO_PATH=./umbrella/build/bin/ohosArm64/debugShared/libumbrella.so
HEADER_PATH=./umbrella/build/bin/ohosArm64/debugShared/libumbrella_api.h
# HAR 增量编译架构：原生产物拷到 shared HAR；entry 仅作 HAP 壳
SHARED_LIB_DIR=./ohosApp/shared/libs/arm64-v8a
SHARED_CPP_DIR=./ohosApp/shared/src/main/cpp
ENTRY_HAR_PATH=./ohosApp/entry/build/default/outputs/default/entry.har
ENTRY_SIGNED_HAP_PATH=./ohosApp/entry/build/default/outputs/default/entry-default-signed.hap
ENTRY_UNSIGNED_HAP_PATH=./ohosApp/entry/build/default/outputs/default/entry-default-unsigned.hap
ARCHIVE_DIR=./archives
# KNI 产物已直接生成到 shared/src/main/ets/kniogen
KNIOGEN_DIR=./ohosApp/shared/src/main/ets/kniogen
TARGET_RES_DIR=./wsCompose/src/commonMain/composeResources/
OHOS_RES_DIR=./ohosApp/entry/src/main/resources/resfile/

# 检查编译产物是否存在
if [ ! -f "$SO_PATH" ]; then
    echo "错误：libumbrella.so 不存在，编译可能失败！"
    exit 1
fi

if [ ! -f "$HEADER_PATH" ]; then
    echo "错误：libumbrella_api.h 不存在，编译可能失败！"
    exit 1
fi

mkdir -p "$ARCHIVE_DIR"
mkdir -p "$SHARED_LIB_DIR"
mkdir -p "$SHARED_CPP_DIR"
rm -f "$ARCHIVE_DIR/libumbrella.so" "$ARCHIVE_DIR/libumbrella_api.h" "$ARCHIVE_DIR/weseecore.har" "$ARCHIVE_DIR/WeSeeCore-phone-debug-signed.hap" "$ARCHIVE_DIR/WeSeeCore-debug-unite.zip"

# 合并各模块子目录下的 knoi 产物到 kniogen 目录，并同步归档一份
sh scripts/merge_kniogen.sh "$KNIOGEN_DIR" "$ARCHIVE_DIR"

cp "$SO_PATH" "$SHARED_LIB_DIR/"
cp "$HEADER_PATH" "$SHARED_CPP_DIR/"
# CMakeLists.txt (shared) 通过相对路径引用 entry/libs 下的 libumbrella.so
ENTRY_LIB_DIR=./ohosApp/entry/libs/arm64-v8a
mkdir -p "$ENTRY_LIB_DIR"
cp "$SO_PATH" "$ENTRY_LIB_DIR/"
cp "$SO_PATH" "$ARCHIVE_DIR/"
cp "$HEADER_PATH" "$ARCHIVE_DIR/"

rm -rf "$ENTRY_HAR_PATH" "$ENTRY_SIGNED_HAP_PATH" "$ENTRY_UNSIGNED_HAP_PATH"

# 拷贝资源文件
rm -rf "$OHOS_RES_DIR"
cp -rf "$TARGET_RES_DIR" "$OHOS_RES_DIR"
echo "拷贝 res 文件成功"

pushd "ohosApp"

echo "ohpm install begin 0"
ohpm clean
echo "ohpm install begin 1"
ohpm config set registry https://ohpm.openharmony.cn/ohpm/,https://ohpm.woa.com/repos/ohpm,https://repo.harmonyos.com/ohpm,https://mirrors.tencent.com/ohpm/tencent_ohpm/public/,http://ohpm-beta.mirrors.woa.com/repos/ohpm
ohpm install
echo "ohpm install begin 2"

hvigorw --sync -p product=default --analyze=normal --parallel
hvigorw --mode module -p module=entry@default -p product=default -p requiredDeviceType=phone assembleHap --analyze=normal --parallel --incremental --no-daemon
popd

if [ -f "$ENTRY_SIGNED_HAP_PATH" ]; then
    cp "$ENTRY_SIGNED_HAP_PATH" "$ARCHIVE_DIR/WeSeeCore-phone-debug-signed.hap"
elif [ -f "$ENTRY_UNSIGNED_HAP_PATH" ]; then
    # CI 流水线场景：无本地签名配置，输出 unsigned hap 供外部签名插件处理
    cp "$ENTRY_UNSIGNED_HAP_PATH" "$ARCHIVE_DIR/WeSeeCore-phone-unsigned.hap"
    echo "已生成 unsigned hap，等待外部签名：$ARCHIVE_DIR/WeSeeCore-phone-unsigned.hap"
else
    echo "错误：HAP 产物不存在，鸿蒙 HAP 打包失败！"
    exit 1
fi

if [ -f "$ENTRY_HAR_PATH" ]; then
    cp "$ENTRY_HAR_PATH" "$ARCHIVE_DIR/weseecore.har"
fi

pushd "$ARCHIVE_DIR"
zip -r WeSeeCore-debug-unite.zip ./
popd

echo "鸿蒙 debug HAP 已生成：$ARCHIVE_DIR/WeSeeCore-phone-debug-signed.hap"
echo "鸿蒙 debug 归档包已生成：$ARCHIVE_DIR/WeSeeCore-debug-unite.zip"
