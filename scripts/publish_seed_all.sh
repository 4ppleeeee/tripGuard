#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ "$(uname -s)" == "Darwin" ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
    export PATH="$JAVA_HOME/bin:$PATH"
fi

: "${kuiklyBizVersion:?请先设置统一版本号，例如 1.0.0.4-dev-aatroxli}"
: "${mavenUserName:?缺少 mavenUserName}"
: "${mavenPassword:?缺少 mavenPassword}"

run_stage() {
    local name="$1"
    shift
    echo
    echo "========== 开始: ${name} =========="
    "$@"
    echo "========== 完成: ${name} =========="
}

echo "========================================"
echo "Seed Core 发布版本: ${kuiklyBizVersion}"
echo "Java: $(java -version 2>&1 | head -n 1)"
echo "========================================"

# 三端各自使用独立的 Kotlin 工具链。任何 leaf 失败都会阻止 root metadata 发布。
run_stage "Android leaf" bash ./scripts/publish_seed_android.sh
run_stage "iOS leaf" bash ./scripts/build_ios_release.sh
run_stage "OHOS leaf" bash ./scripts/publish_seed_ohos.sh

# root metadata 只能最后发布一次，作为三端 leaf 的统一路由入口。
run_stage "KMP root metadata" ./gradlew :umbrella:publishSeedMetadataArtifact \
    --no-daemon \
    --console=plain

echo
echo "Seed Core 三端 Maven 发布完成: ${kuiklyBizVersion}"
