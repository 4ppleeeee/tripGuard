#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

# 写入流水线信息到 local.properties，供 BuildConfig 注入
LOCAL_PROPS="./local.properties"
{
    echo "ci.build.id=${BK_CI_BUILD_ID:-local}"
    echo "ci.build.num=${BK_CI_BUILD_NUM:-local}"
    echo "ci.pipeline.name=${BK_CI_PIPELINE_NAME:-local}"
    echo "ci.branch=${BK_CI_GIT_REPO_BRANCH:-local}"
    echo "ci.commit=${BK_CI_GIT_COMMIT_ID:-local}"
    echo "ci.build.time=$(date '+%Y-%m-%d %H:%M:%S')"
} >> "$LOCAL_PROPS"

./gradlew :startup:checkSchemeDoc :androidApp:assembleRelease

mkdir -p ./bin/apk
cp androidApp/build/outputs/mapping/*/*.txt ./bin/
cp androidApp/build/outputs/apk/release/*.apk ./bin/apk/
