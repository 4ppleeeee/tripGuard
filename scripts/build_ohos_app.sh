#!/bin/sh

echo "working path: $(pwd)"
weseeOhosApp=`grep "^qqnews.ohos.app.dir" local.properties | awk -F'=' '{print $2}'`

if [ -z "$weseeOhosApp" ]; then
    echo "WeSee ohos project dir is empty, stopping the script."
    exit 1
fi

export JAVA_HOME=${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}
export PATH=$JAVA_HOME/bin:$PATH
./gradlew :startup:checkSchemeDoc || exit 1

pushd $weseeOhosApp

SDK_HOME=/Applications/DevEco-Studio.app/Contents
export DEVECO_SDK_HOME=$SDK_HOME/sdk
export PATH=$DEVECO_SDK_HOME:$SDK_HOME/jbr/Contents/Home/bin:$SDK_HOME/tools/node/bin:$SDK_HOME/tools/ohpm/bin:$SDK_HOME/tools/hvigor/bin:$PATH

$SDK_HOME/tools/node/bin/node $SDK_HOME/tools/hvigor/bin/hvigorw.js \
  --mode module -p module=entry@default -p product=default -p requiredDeviceType=phone assembleHap --analyze=normal --parallel

HDC_BIN=$SDK_HOME/sdk/default/openharmony/toolchains/hdc
targets=$($HDC_BIN list targets)
HAP_PATH=entry/build/default/outputs/default
if [ -z "$targets" ]; then
  echo "error: 先启动模拟器"
  exit 1
elif [ -e "$HAP_PATH/entry-default-unsigned.hap" ] && [ ! -e "$HAP_PATH/entry-default-signed.hap" ]; then
  echo "error: 先配置签名"
  exit 2
else
  target_id=$(echo "$targets" | head -n 1)
  $HDC_BIN -t "$target_id" shell aa force-stop com.tencent.weseecore
  $HDC_BIN -t "$target_id" install entry/build/default/outputs/default/entry-default-signed.hap
  $HDC_BIN -t "$target_id" shell aa start -a EntryAbility -b com.tencent.weseecore
fi
