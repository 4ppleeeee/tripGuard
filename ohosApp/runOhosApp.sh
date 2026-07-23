#!/bin/sh
set -e

echo "working path: $(pwd)"
echo "=== KMM Core 鸿蒙构建 (HAR 增量编译架构) ==="

export QN_COMPAT_BUILD_TYPE=ohos
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# ===== 配置 =====
ENTRY_DIR="./ohosApp/entry"

# hvigor 环境
SDK_HOME=/Applications/DevEco-Studio.app/Contents
export DEVECO_SDK_HOME=$SDK_HOME/sdk
export PATH=$DEVECO_SDK_HOME:$SDK_HOME/jbr/Contents/Home/bin:$SDK_HOME/tools/node/bin:$SDK_HOME/tools/ohpm/bin:$SDK_HOME/tools/hvigor/bin:$PATH
OHPM_BIN=$SDK_HOME/tools/ohpm/bin/ohpm
NODE_BIN=$SDK_HOME/tools/node/bin/node
HVIGOR_BIN=$SDK_HOME/tools/hvigor/bin/hvigorw.js

# 说明：
# KMP so 的编译与拷贝由 entry/hvigorfile.ts 中的 kuiklyCompilePlugin() 完成：
#   - 执行 ./gradlew :umbrella:linkDebugSharedOhosArm64
#   - 按 ohosApp/local.properties 的 kuikly.soPath / kuikly.headerPath 拷贝到 shared/
# hvigor --sync 由 assembleHap 入口自动完成，无需显式调用。
# 因此本脚本仅保留：历史残留清理 -> 资源拷贝 -> ohpm install -> assembleHap -> 安装。

# ===== Step2: 拷贝资源文件到 entry =====
echo ""
echo "Step2: 拷贝资源文件..."
TARGET_RES_FILE="./wsCompose/src/commonMain/composeResources/"
ENTRY_RES_FILE="$ENTRY_DIR/src/main/resources/resfile/"
if [ -d "$TARGET_RES_FILE" ]; then
  rm -rf "$ENTRY_RES_FILE"
  cp -rf "$TARGET_RES_FILE" "$ENTRY_RES_FILE"
  echo "  ✅ 拷贝资源文件成功"
fi

# ===== Step3: hvigor 构建（增量）======
# 构建 entry HAP：
#   - kuiklyCompilePlugin() 会在 PreBuild 前触发 KMP so 编译与拷贝
#   - hvigor 自动处理 shared HAR 依赖、sync 与增量编译
echo ""
echo "Step3: hvigor 构建..."
pushd ohosApp

$OHPM_BIN install --all

$NODE_BIN $HVIGOR_BIN \
  --mode module \
  -p module=entry@default \
  -p product=default \
  -p requiredDeviceType=phone \
  assembleHap \
  --analyze=normal \
  --parallel \
  --incremental \
  --daemon

popd
echo "✅ 构建成功"

# ===== Step4: 安装到设备 ======
echo ""
echo "Step4: 安装到设备..."
pushd ohosApp
HDC_BIN=$SDK_HOME/sdk/default/openharmony/toolchains/hdc
TARGETS=$($HDC_BIN list targets)
HAP_PATH=entry/build/default/outputs/default

if [ -z "$TARGETS" ]; then
  echo "error: 先启动模拟器"
  exit 1
elif [ -e "$HAP_PATH/entry-default-unsigned.hap" ] && [ ! -e "$HAP_PATH/entry-default-signed.hap" ]; then
  echo "error: 先配置签名 参考: https://developer.huawei.com/consumer/cn/doc/HMSCore-Guides/harmonyos-java-config-app-signing-0000001199536987"
  exit 2
else
  for target_id in $($HDC_BIN list targets); do
    echo "  安装到 $target_id"
    $HDC_BIN -t "$target_id" shell aa force-stop com.tencent.news.base.app
    $HDC_BIN -t "$target_id" install entry/build/default/outputs/default/entry-default-signed.hap
    $HDC_BIN -t "$target_id" shell aa start -a EntryAbility -b com.tencent.news.base.app
  done
fi

popd
echo ""
echo "🎉 安装成功！"
