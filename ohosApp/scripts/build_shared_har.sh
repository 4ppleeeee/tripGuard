#!/bin/sh
set -e

echo "=== 构建 shared HAR (包含 KMP so) ==="
echo "working path: $(pwd)"

export QN_COMPAT_BUILD_TYPE=ohos
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# ====== Step1: Gradle 编译 umbrella so ======
echo "Step1: 编译 KMP so..."
./gradlew :umbrella:linkDebugSharedOhosArm64 \
  -Pqqnews.kmm.build.platform=ohos \
  -Pdebug.mode=true \
  --build-cache \
  --parallel

# ====== Step2: 拷贝 so 和头文件到 shared 模块 ======
echo "Step2: 拷贝 so 和头文件到 shared 模块..."
mkdir -p ohosApp/shared/libs/arm64-v8a
mkdir -p ohosApp/shared/src/main/cpp

SO_SRC=./umbrella/build/bin/ohosArm64/debugShared/libumbrella.so
HDR_SRC=./umbrella/build/bin/ohosArm64/debugShared/libumbrella_api.h

if [ -f "$SO_SRC" ]; then
  cp "$SO_SRC" ohosApp/shared/libs/arm64-v8a/
  echo "  拷贝 libumbrella.so 成功"
else
  echo "❌ 错误：SO 文件不存在: $SO_SRC"
  exit 1
fi

if [ -f "$HDR_SRC" ]; then
  # 对齐 CMakeLists.txt 的 include 搜索路径（cpp 根目录），
  # napi_init.cpp 里 #include "libumbrella_api.h" 会优先从此处读取。
  cp "$HDR_SRC" ohosApp/shared/src/main/cpp/
  echo "  拷贝 libumbrella_api.h 成功"
else
  echo "❌ 错误：头文件不存在: $HDR_SRC"
  exit 1
fi

# ====== Step3: 拷贝资源文件到 shared 模块 ======
echo "Step3: 拷贝资源文件到 shared 模块..."
TARGET_RES_FILE="./wsCompose/src/commonMain/composeResources/"
SHARED_RES_FILE="./ohosApp/shared/src/main/resources/resfile/"

if [ -d "$TARGET_RES_FILE" ]; then
  rm -rf "$SHARED_RES_FILE"
  cp -rf "$TARGET_RES_FILE" "$SHARED_RES_FILE"
  echo "  拷贝资源文件成功"
else
  echo "⚠️  警告：资源目录不存在: $TARGET_RES_FILE"
fi

# ====== Step4: 合并 kniogen 产物到 shared 模块 ======
echo "Step4: 合并 kniogen 产物..."
KNIOGEN_DIR=./ohosApp/shared/src/main/ets/kniogen
if [ -d "$KNIOGEN_DIR" ]; then
  sh scripts/merge_kniogen.sh "$KNIOGEN_DIR"
  echo "  合并 kniogen 成功"
else
  echo "⚠️  警告：kniogen 目录不存在: $KNIOGEN_DIR"
fi

# ====== Step5: 构建 shared HAR ======
echo "Step5: 构建 shared HAR..."
pushd ohosApp
SDK_HOME=/Applications/DevEco-Studio.app/Contents
export DEVECO_SDK_HOME=$SDK_HOME/sdk
export PATH=$DEVECO_SDK_HOME:$SDK_HOME/jbr/Contents/Home/bin:$SDK_HOME/tools/node/bin:$SDK_HOME/tools/ohpm/bin:$SDK_HOME/tools/hvigor/bin:$PATH

$SDK_HOME/tools/ohpm/bin/ohpm install --all

$SDK_HOME/tools/node/bin/node $SDK_HOME/tools/hvigor/bin/hvigorw.js \
  --mode module \
  -p module=shared@default \
  -p product=default \
  assembleHar \
  --analyze=normal \
  --parallel \
  --incremental \
  --daemon

popd

echo "✅ shared HAR 构建成功"
echo "   HAR 路径: ohosApp/shared/build/default/outputs/default/shared.har"
