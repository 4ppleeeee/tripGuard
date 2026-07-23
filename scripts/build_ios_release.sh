#!/bin/bash
set -euo pipefail

# iOS 发布构建脚本
# 用法:
#   ./scripts/build_ios_release.sh                    # 发布所有架构: arm64 + simulatorArm64 + x64
#   ./scripts/build_ios_release.sh arm64              # 仅发布真机 arm64
#   ./scripts/build_ios_release.sh debug              # 禁用优化(包括 LTO)
#   ./scripts/build_ios_release.sh arm64 debug        # 仅发布真机 arm64 并禁用优化
#   ./scripts/build_ios_release.sh nocache            # 禁用 Kotlin/Native 缓存

export QN_COMPAT_BUILD_TYPE=ios
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"

ARM64_ONLY=false
DISABLE_OPTIMIZATION=false
NO_CACHE=false

for arg in "$@"; do
    case "$arg" in
        arm64)
            ARM64_ONLY=true
            ;;
        debug)
            DISABLE_OPTIMIZATION=true
            ;;
        nocache)
            NO_CACHE=true
            ;;
        *)
            echo "Unsupported argument: $arg" >&2
            exit 1
            ;;
    esac
done

GRADLE_ARGS=(
    "-Pkmm.seed.fat=true"
    "-Pqn.ios.arm64Only=$ARM64_ONLY"
    "-Pqn.ios.disableOptimization=$DISABLE_OPTIMIZATION"
)

if [ "$NO_CACHE" = true ]; then
    GRADLE_ARGS+=(
        "-Pkotlin.native.cacheKind.iosArm64=none"
        "-Pkotlin.native.cacheKind.iosX64=none"
        "-Pkotlin.native.cacheKind.iosSimulatorArm64=none"
    )
fi

echo "iOS publish configuration: arm64Only=$ARM64_ONLY, disableOptimization=$DISABLE_OPTIMIZATION, noCache=$NO_CACHE"

./gradlew :umbrella:publishSeedIosArtifact "${GRADLE_ARGS[@]}" --no-daemon --console=plain --stacktrace
