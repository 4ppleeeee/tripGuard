#!/bin/bash
# 仅用来检查鸿蒙编译so是否成功，编译出的so是debug版本
export QN_COMPAT_BUILD_TYPE=ohos
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
./gradlew :startup:checkSchemeDoc :umbrella:linkDebugSharedOhosArm64 -Pqqnews.kmm.build.platform=ohos || exit 1
# 检查编译产物是否存在
if [ ! -f "./umbrella/build/bin/ohosArm64/debugShared/libumbrella.so" ]; then
    echo "错误：libumbrella.so 不存在，编译可能失败！"
    exit 1
fi

if [ ! -f "./umbrella/build/bin/ohosArm64/debugShared/libumbrella_api.h" ]; then
    echo "错误：libumbrella_api.h 不存在，编译可能失败！"
    exit 1
fi
mkdir -p archives

KNIOGEN_DIR=./ohosApp/shared/src/main/ets/kniogen

# 合并各模块子目录下的 knoi 产物到 archives 目录
sh scripts/merge_kniogen.sh "$KNIOGEN_DIR" "./archives"
cp ./umbrella/build/bin/ohosArm64/debugShared/libumbrella.so archives/
cp ./umbrella/build/bin/ohosArm64/debugShared/libumbrella_api.h archives/
zip -r WeSeeCore-debug-kmm-so.zip archives/
