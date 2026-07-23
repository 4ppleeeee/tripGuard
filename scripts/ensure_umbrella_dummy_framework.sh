#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
FRAMEWORK_DIR="$ROOT_DIR/umbrella/build/cocoapods/framework/umbrella.framework"

framework_ready() {
    [ -d "$FRAMEWORK_DIR" ] && find "$FRAMEWORK_DIR" -mindepth 1 -print -quit >/dev/null 2>&1
}

if [ -z "${JAVA_HOME:-}" ] && [ -x /usr/libexec/java_home ]; then
    JAVA_HOME_CANDIDATE=$(/usr/libexec/java_home -v 17 2>/dev/null || true)
    if [ -n "$JAVA_HOME_CANDIDATE" ]; then
        export JAVA_HOME="$JAVA_HOME_CANDIDATE"
    fi
fi

if [ -n "${JAVA_HOME:-}" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

export QN_COMPAT_BUILD_TYPE="${QN_COMPAT_BUILD_TYPE:-ios}"

if framework_ready; then
    echo "🧱 umbrella.framework 已存在，跳过 dummy framework 生成"
    exit 0
fi

echo "🧱 umbrella.framework 不存在或为空，执行 :umbrella:generateDummyFramework..."
(
    cd "$ROOT_DIR"
    ./gradlew :startup:checkSchemeDoc :umbrella:generateDummyFramework
)

if framework_ready; then
    echo "✅ 已生成 umbrella dummy framework: $FRAMEWORK_DIR"
else
    echo "❌ :umbrella:generateDummyFramework 执行后仍未生成有效的 umbrella.framework"
    exit 1
fi
