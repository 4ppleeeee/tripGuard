#!/bin/bash
# 仅用来检查鸿蒙编译 so 是否成功，额外生成 release HAR 和 HAP 产物
set -e
export QN_COMPAT_BUILD_TYPE=ohos
export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}
export PATH=$JAVA_HOME/bin:$PATH

./gradlew :startup:checkSchemeDoc :umbrella:linkReleaseSharedOhosArm64 -Pqqnews.kmm.build.platform=ohos -Pdebug.mode=false

SO_PATH=./umbrella/build/bin/ohosArm64/releaseShared/libumbrella.so
HEADER_PATH=./umbrella/build/bin/ohosArm64/releaseShared/libumbrella_api.h
# HAR 增量编译架构：原生产物拷到 shared HAR；entry 仅作 HAP 壳
SHARED_LIB_DIR=./ohosApp/shared/libs/arm64-v8a
SHARED_CPP_DIR=./ohosApp/shared/src/main/cpp
ENTRY_LIB_DIR=./ohosApp/entry/libs/arm64-v8a
SHARED_HAR_PATH=./ohosApp/shared/build/default/outputs/default/shared.har
ENTRY_SIGNED_HAP_PATH=./ohosApp/entry/build/default/outputs/default/entry-default-signed.hap
ENTRY_UNSIGNED_HAP_PATH=./ohosApp/entry/build/default/outputs/default/entry-default-unsigned.hap
ARCHIVE_DIR=./archives
# KNI 产物已直接生成到 shared/src/main/ets/kniogen
KNIOGEN_DIR=./ohosApp/shared/src/main/ets/kniogen
TARGET_RES_DIR=./wsCompose/src/commonMain/composeResources/
OHOS_RES_DIR=./ohosApp/entry/src/main/resources/resfile/

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
mkdir -p "$ENTRY_LIB_DIR"
rm -f "$ARCHIVE_DIR/libumbrella.so" "$ARCHIVE_DIR/libumbrella_api.h" "$ARCHIVE_DIR/weseecore.har" "$ARCHIVE_DIR/WeSeeCore-phone-release-signed.hap" "$ARCHIVE_DIR/WeSeeCore-phone-unsigned.hap"

# 合并各模块子目录下的 knoi 产物到 kniogen 目录，并同步归档一份
sh scripts/merge_kniogen.sh "$KNIOGEN_DIR" "$ARCHIVE_DIR"

cp "$SO_PATH" "$SHARED_LIB_DIR/"
cp "$HEADER_PATH" "$SHARED_CPP_DIR/"
cp "$SO_PATH" "$ENTRY_LIB_DIR/"
cp "$SO_PATH" "$ARCHIVE_DIR/"
cp "$HEADER_PATH" "$ARCHIVE_DIR/"

rm -rf "$SHARED_HAR_PATH" "$ENTRY_SIGNED_HAP_PATH" "$ENTRY_UNSIGNED_HAP_PATH"

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
if [ $? -ne 0 ]; then
    exit 1
fi
hvigorw --mode module -p product=default -p module=shared@default -p buildMode="release" assembleHar --analyze=normal --parallel --incremental --no-daemon
hvigorw --mode module -p product=default -p module=entry@default -p buildMode="release" -p requiredDeviceType=phone assembleHap --analyze=normal --parallel --incremental --no-daemon
popd

if [ ! -f "$SHARED_HAR_PATH" ]; then
    echo "错误：shared.har 不存在，鸿蒙 HAR 打包失败！"
    exit 1
fi

if [ -f "$ENTRY_SIGNED_HAP_PATH" ]; then
    HAP_ARCHIVE_PATH="$ARCHIVE_DIR/WeSeeCore-phone-release-signed.hap"
    cp "$ENTRY_SIGNED_HAP_PATH" "$HAP_ARCHIVE_PATH"
elif [ -f "$ENTRY_UNSIGNED_HAP_PATH" ]; then
    HAP_ARCHIVE_PATH="$ARCHIVE_DIR/WeSeeCore-phone-unsigned.hap"
    cp "$ENTRY_UNSIGNED_HAP_PATH" "$HAP_ARCHIVE_PATH"
    echo "已生成 unsigned hap，等待外部签名：$HAP_ARCHIVE_PATH"
else
    echo "错误：HAP 产物不存在，鸿蒙 HAP 打包失败！"
    exit 1
fi

cp "$SHARED_HAR_PATH" "$ARCHIVE_DIR/weseecore.har"

echo "鸿蒙 release 联合产物已生成：$ARCHIVE_DIR/weseecore.har"
echo "鸿蒙 release HAP 已生成：$HAP_ARCHIVE_PATH"
