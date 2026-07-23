#!/bin/bash
# 编译出的so是release版本，耗时较长，预计15-20分钟
# HAR 增量编译架构：原生产物拷到 shared HAR，KNI 产物已直接生成到 shared
export QN_COMPAT_BUILD_TYPE=ohos
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
./gradlew :startup:checkSchemeDoc :umbrella:linkReleaseSharedOhosArm64 -Pqqnews.kmm.build.platform=ohos || exit 1
KNIOGEN_DIR=./ohosApp/shared/src/main/ets/kniogen

# 合并各模块子目录下的 knoi 产物到 kniogen 目录
sh scripts/merge_kniogen.sh "$KNIOGEN_DIR"
mkdir -p ohosApp/shared/libs/arm64-v8a
mkdir -p ohosApp/shared/src/main/cpp
cp ./umbrella/build/bin/ohosArm64/umbrellaReleaseShared/libumbrella.so ohosApp/shared/libs/arm64-v8a/
cp ./umbrella/build/bin/ohosArm64/umbrellaReleaseShared/libumbrella_api.h ohosApp/shared/src/main/cpp/
git add ohosApp/shared/src/main/ets/kniogen/callback.ets
git add ohosApp/shared/src/main/ets/kniogen/consumer.ets
git add ohosApp/shared/src/main/ets/kniogen/provider.ets
git add ohosApp/shared/libs/arm64-v8a/libumbrella.so
git add ohosApp/shared/src/main/cpp/libumbrella_api.h
git commit -m "update Harmony kmm so"
git push
