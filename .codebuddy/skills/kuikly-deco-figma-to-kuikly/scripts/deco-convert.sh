#!/usr/bin/env bash
# ============================================================
# deco-convert.sh — Deco D2C 一键转码（跨平台通用版）
# 用法: bash deco-convert.sh <figma-url> [--dsl compose|traditional] [--remote]
#
# 参数:
#   <figma-url>                Figma 链接（必须含 node-id）
#   --dsl compose|traditional  指定 DSL 类型（可选，默认自动推断）
#   --remote                   强制使用远程模式（可选）
#
# 支持平台: macOS / Linux / Windows (WSL/Git Bash)
# 返回: 0 = 转码成功, 非 0 = 失败
# ============================================================
set -euo pipefail

# ---- 平台检测 ----
detect_os() {
    case "$(uname -s)" in
        Darwin*)  OS_TYPE="macos" ;;
        Linux*)   OS_TYPE="linux" ;;
        MINGW*|MSYS*|CYGWIN*) OS_TYPE="windows" ;;
        *)        OS_TYPE="unknown" ;;
    esac
}
detect_os

# ---- 颜色（检测终端是否支持） ----
if [ -t 1 ] && command -v tput &>/dev/null && [ "$(tput colors 2>/dev/null || echo 0)" -ge 8 ]; then
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    CYAN='\033[0;36m'
    NC='\033[0m'
else
    RED='' GREEN='' YELLOW='' CYAN='' NC=''
fi

ok()   { echo -e "${GREEN}✔${NC} $*"; }
warn() { echo -e "${YELLOW}⚠${NC} $*"; }
fail() { echo -e "${RED}✗${NC} $*"; }
info() { echo -e "${CYAN}ℹ${NC} $*"; }

# ---- 跨平台工具函数 ----

# 跨平台 sed -i（macOS BSD sed vs GNU sed）
portable_sed_i() {
    local expression="$1"
    local file="$2"
    if [ "$OS_TYPE" = "macos" ]; then
        sed -i '' "$expression" "$file"
    else
        sed -i "$expression" "$file"
    fi
}

# 跨平台 mktemp
portable_mktemp() {
    if [ "$OS_TYPE" = "macos" ]; then
        mktemp /tmp/deco-output.XXXXXX
    else
        mktemp --tmpdir deco-output.XXXXXX
    fi
}

# 跨平台 TCP 端口探测
portable_tcp_check() {
    local host="$1" port="$2" timeout="${3:-3}"
    if command -v nc &>/dev/null; then
        nc -z -w "$timeout" "$host" "$port" 2>/dev/null
    elif command -v bash &>/dev/null && [ -e /dev/tcp ]; then
        (echo >/dev/tcp/"$host"/"$port") 2>/dev/null
    elif command -v timeout &>/dev/null; then
        timeout "$timeout" bash -c "echo >/dev/tcp/$host/$port" 2>/dev/null
    else
        # 回退到 curl
        curl -sf --connect-timeout "$timeout" "http://$host:$port" &>/dev/null
    fi
}

# ---- 默认值 ----
FIGMA_URL=""
DSL_TYPE=""         # compose / traditional / 空=自动推断
FORCE_REMOTE=false
REMOTE_FLAG=""      # 初始化，避免 set -u 下 unbound variable
HEARTBEAT_PID=""    # 心跳进程 PID
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 确保脚本退出时清理心跳进程
cleanup() {
    if [ -n "${HEARTBEAT_PID:-}" ] && kill -0 "$HEARTBEAT_PID" 2>/dev/null; then
        kill "$HEARTBEAT_PID" 2>/dev/null
        wait "$HEARTBEAT_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT

# ============================================================
# 解析参数
# ============================================================
parse_args() {
    while [ $# -gt 0 ]; do
        case "$1" in
            --dsl)
                shift
                DSL_TYPE="${1:-}"
                if [[ "$DSL_TYPE" != "compose" && "$DSL_TYPE" != "traditional" ]]; then
                    fail "--dsl 参数必须为 compose 或 traditional"
                    exit 1
                fi
                ;;
            --remote)
                FORCE_REMOTE=true
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                if [ -z "$FIGMA_URL" ]; then
                    FIGMA_URL="$1"
                else
                    fail "未知参数: $1"
                    usage
                    exit 1
                fi
                ;;
        esac
        shift
    done

    if [ -z "$FIGMA_URL" ]; then
        fail "缺少 Figma URL"
        usage
        exit 1
    fi
}

usage() {
    echo "用法: bash $(basename "$0") <figma-url> [选项]"
    echo ""
    echo "选项:"
    echo "  --dsl compose|traditional   指定目标 DSL 类型（默认自动推断）"
    echo "  --remote                    强制远程模式"
    echo "  -h, --help                  显示帮助"
    echo ""
    echo "支持平台: macOS / Linux / Windows (WSL/Git Bash)"
}

# ============================================================
# Step 1: 环境检查
# ============================================================
step_env_check() {
    info "Step 1: 环境检查"
    if [ -f "$SCRIPT_DIR/deco-env-check.sh" ]; then
        bash "$SCRIPT_DIR/deco-env-check.sh"
    else
        # 最小检查
        command -v node &>/dev/null || { fail "Node.js 未安装"; exit 1; }
        command -v npm &>/dev/null  || { fail "npm 未安装"; exit 1; }
        command -v deco &>/dev/null || { fail "Deco CLI 未安装"; exit 1; }
        ok "环境检查通过（最小检查）"
    fi
}

# ============================================================
# Step 2: 校验 Figma URL
# ============================================================
step_validate_url() {
    info "Step 2: 校验 Figma URL"

    # 检查是否包含 figma.com
    if [[ "$FIGMA_URL" != *"figma.com"* ]]; then
        fail "无效的 Figma URL（必须包含 figma.com）"
        exit 1
    fi

    # 检查 node-id 参数
    if [[ "$FIGMA_URL" != *"node-id="* ]]; then
        fail "Figma URL 缺少 node-id 参数"
        echo ""
        echo "  正确获取方式："
        echo "  1. 在 Figma 中选中目标 Frame 或 Component（不要选 Group）"
        echo "  2. 右键 → Copy link to selection"
        echo "  3. 粘贴得到的 URL 应包含 ?node-id=xxx"
        exit 1
    fi

    ok "Figma URL 校验通过"
}

# ============================================================
# Step 3: 判断目标 DSL 类型
# ============================================================
step_detect_dsl() {
    info "Step 3: 判断目标 DSL 类型"

    if [ -n "$DSL_TYPE" ]; then
        ok "用户指定 DSL 类型: ${DSL_TYPE}"
        return
    fi

    # 自动推断：扫描项目代码
    local has_compose=false
    local has_traditional=false
    local search_dir="${PROJECT_DIR:-$(pwd)}"

    if grep -rq "@Composable" "$search_dir" --include="*.kt" 2>/dev/null; then
        has_compose=true
    fi
    if grep -rq "attr {" "$search_dir" --include="*.kt" 2>/dev/null; then
        has_traditional=true
    fi

    if $has_compose && ! $has_traditional; then
        DSL_TYPE="compose"
        ok "自动推断: Compose DSL（发现 @Composable 注解）"
    elif $has_traditional && ! $has_compose; then
        DSL_TYPE="traditional"
        ok "自动推断: 传统 DSL（发现 attr { } 模式）"
    elif $has_compose && $has_traditional; then
        DSL_TYPE="compose"
        ok "自动推断: Compose DSL（两种都存在，默认 Compose）"
    else
        DSL_TYPE="traditional"
        ok "自动推断: 传统 DSL（未发现任何模式，默认传统 DSL）"
    fi
}

# ============================================================
# Step 4: 判断转码模式（本地/远程）
# ============================================================
step_detect_mode() {
    info "Step 4: 判断转码模式"

    if $FORCE_REMOTE; then
        REMOTE_FLAG="--remote"
        ok "强制远程模式（用户指定 --remote）"
        return
    fi

    # 探测本地 Figma Desktop MCP（端口 3845）
    if portable_tcp_check 127.0.0.1 3845; then
        REMOTE_FLAG=""
        ok "本地模式（Figma Desktop MCP 已连接，端口 3845 可达）"
    else
        REMOTE_FLAG="--remote"
        ok "远程模式（本地 MCP 不可用，端口 3845 不可达）"
    fi
}

# ============================================================
# Step 5: 执行转码（带心跳输出，防止 Agent 误判超时）
# ============================================================

# 心跳进程：每 15 秒输出一行存活信号，让 Agent 知道任务还在运行
start_heartbeat() {
    local start_time=$SECONDS
    (
        while true; do
            sleep 15
            local elapsed=$(( SECONDS - start_time ))
            echo -e "${CYAN}⏳${NC} 转码进行中... 已耗时 ${elapsed}s"
        done
    ) &
    HEARTBEAT_PID=$!
}

stop_heartbeat() {
    if [ -n "${HEARTBEAT_PID:-}" ] && kill -0 "$HEARTBEAT_PID" 2>/dev/null; then
        kill "$HEARTBEAT_PID" 2>/dev/null
        wait "$HEARTBEAT_PID" 2>/dev/null || true
    fi
    HEARTBEAT_PID=""
}

# 执行 deco 命令：实时流式输出 + 心跳 + 捕获输出用于后续检查
run_deco_cmd() {
    local full_cmd="$1"
    local tmp_output
    tmp_output=$(portable_mktemp)

    # 启动心跳
    start_heartbeat

    # 用 tee 实时输出的同时捕获到临时文件
    set +e
    eval "$full_cmd" 2>&1 | tee "$tmp_output"
    local pipe_status=("${PIPESTATUS[@]}")
    set -e

    # 停止心跳
    stop_heartbeat

    # pipe_status[0] 是 deco 命令的退出码
    DECO_EXIT_CODE=${pipe_status[0]}

    # 将输出存入全局变量供调用者检查
    DECO_OUTPUT=$(cat "$tmp_output")
    rm -f "$tmp_output"
}

# 探测 deco output 目录（通用多路径策略）
find_deco_output_base() {
    local candidates=()

    # 1. 从 npm root -g 推断（最通用）
    local npm_root
    npm_root=$(npm root -g 2>/dev/null || echo "")
    if [ -n "$npm_root" ]; then
        candidates+=("$npm_root/@tencent/deco/output")
    fi

    # 2. Volta 全局包路径
    if [ -d "$HOME/.volta" ]; then
        # volta 的全局包在 packages 目录而非 node_modules
        local volta_pkg_dir="$HOME/.volta/tools/image/packages/@tencent/deco/output"
        candidates+=("$volta_pkg_dir")
    fi

    # 3. nvm 路径
    local node_ver
    node_ver=$(node -v 2>/dev/null || echo "")
    if [ -n "$node_ver" ] && [ -d "$HOME/.nvm" ]; then
        candidates+=("$HOME/.nvm/versions/node/$node_ver/lib/node_modules/@tencent/deco/output")
    fi

    # 4. 常见系统路径
    case "$OS_TYPE" in
        macos)
            candidates+=("/usr/local/lib/node_modules/@tencent/deco/output")
            candidates+=("/opt/homebrew/lib/node_modules/@tencent/deco/output")
            ;;
        linux)
            candidates+=("/usr/lib/node_modules/@tencent/deco/output")
            candidates+=("/usr/local/lib/node_modules/@tencent/deco/output")
            ;;
    esac

    # 返回第一个存在的路径
    for base in "${candidates[@]}"; do
        if [ -d "$base" ]; then
            echo "$base"
            return 0
        fi
    done
    return 1
}

step_convert() {
    info "Step 5: 执行转码"

    # 构建命令
    local cmd
    if [ "$DSL_TYPE" = "compose" ]; then
        cmd="deco to-kuikly"
    else
        cmd="deco to-kuikly-dsl"
    fi

    local full_cmd="$cmd \"$FIGMA_URL\" $REMOTE_FLAG"
    info "执行: $full_cmd"
    info "转码可能需要 30s~2min，请耐心等待..."
    echo ""

    # 初始化全局变量
    DECO_OUTPUT=""
    DECO_EXIT_CODE=0

    # 执行（实时输出 + 心跳）
    run_deco_cmd "$full_cmd"

    echo ""

    # 判断转码结果
    # 策略：优先检查成功标志（✓ 生成成功），有成功标志则忽略退出码
    local has_success=false
    if echo "$DECO_OUTPUT" | grep -q "生成成功"; then
        has_success=true
    fi

    # ---- 认证/登录态过期检测 ----
    # 覆盖以下场景：
    #   1. 明确输出 "未登录" / "未登录 Flowly"
    #   2. 登录态过期导致的图片上传失败（"Flowly Token" 出现在错误提示中）
    #   3. Token 过期相关关键词
    local needs_login=false
    if echo "$DECO_OUTPUT" | grep -qiE "未登录|Flowly Token|token.*过期|token.*expired|token.*invalid|请先登录"; then
        needs_login=true
    fi

    if $needs_login && ! $has_success; then
        warn "检测到登录态过期或未登录，正在自动执行 deco login..."
        echo ""
        echo "  检测依据："
        echo "$DECO_OUTPUT" | grep -iE "未登录|Flowly Token|token|请先登录|图片上传失败" | head -5 | while read -r line; do
            echo "    > $line"
        done
        echo ""

        if deco login 2>&1; then
            ok "登录成功，重新执行转码..."
            echo ""
            run_deco_cmd "$full_cmd"
            echo ""

            if echo "$DECO_OUTPUT" | grep -q "生成成功"; then
                has_success=true
            else
                fail "转码失败（登录后重试仍然失败）"
                echo ""
                echo "  如果问题持续，请检查："
                echo "    1. 网络连接是否正常"
                echo "    2. Deco 服务是否可用"
                echo "    3. 手动执行: deco login"
                exit 1
            fi
        else
            fail "登录失败，请手动执行: deco login"
            exit 1
        fi
    elif ! $has_success && echo "$DECO_OUTPUT" | grep -qE "✗|FAILED|处理失败"; then
        # ---- 未识别的错误：直接失败，不重试 ----
        fail "转码失败"
        echo ""
        echo "  错误摘要："
        echo "$DECO_OUTPUT" | grep -E "✗|FAILED|失败|⚠" | head -10 | while read -r line; do
            echo "    $line"
        done
        echo ""
        echo "  请根据以上错误信息排查问题后重新执行。"
        echo "  故障排查参考: TROUBLESHOOTING.md"
        exit 1
    elif ! $has_success && [ "$DECO_EXIT_CODE" -ne 0 ]; then
        # 退出码非 0 且没有成功标志也没匹配到已知错误模式
        fail "转码异常退出（退出码: $DECO_EXIT_CODE）"
        echo ""
        echo "  未匹配到已知错误模式，请检查以上输出内容。"
        echo "  故障排查参考: TROUBLESHOOTING.md"
        exit 1
    fi

    ok "转码完成 ✅"

    # 输出产物目录（非关键步骤，不应因此导致脚本失败）
    set +e

    local latest_dir=""

    # 策略 1（最可靠）：从 deco 输出中提取产物路径
    if [ -n "$DECO_OUTPUT" ]; then
        local extracted_path
        # 提取 Kotlin 代码路径，取其父目录
        extracted_path=$(echo "$DECO_OUTPUT" | grep -oE '/[^ ]+\.kt$' | head -1)
        if [ -n "$extracted_path" ]; then
            latest_dir=$(dirname "$extracted_path")
        fi
        # 如果没找到 .kt，尝试提取 "输出目录" 或 "输出目录:" 后面的路径
        if [ -z "$latest_dir" ]; then
            extracted_path=$(echo "$DECO_OUTPUT" | grep '输出目录' | grep -oE '/[^ ]+' | head -1)
            if [ -n "$extracted_path" ] && [ -d "$extracted_path" ]; then
                latest_dir=$(ls -td "$extracted_path"/*/ 2>/dev/null | head -1)
            fi
        fi
    fi

    # 策略 2：多路径探测
    if [ -z "$latest_dir" ]; then
        local output_base
        output_base=$(find_deco_output_base)
        if [ -n "$output_base" ]; then
            latest_dir=$(ls -td "$output_base"/*/ 2>/dev/null | head -1)
        fi
    fi

    if [ -n "$latest_dir" ]; then
        echo ""
        ok "产物目录: $latest_dir"
        echo "  文件列表:"
        ls -1 "$latest_dir" 2>/dev/null | while read -r fname; do
            echo "    $fname"
        done
        # 导出供后续脚本使用（用带 PID 的文件名避免并发冲突）
        echo "$latest_dir" > "/tmp/deco-latest-output-$$"
        # 同时写入无后缀版本用于兼容
        echo "$latest_dir" > "/tmp/deco-latest-output"
    else
        warn "未找到产物目录（这不影响转码结果，产物可能在 deco 输出的路径中）"
    fi
    set -e
}

# ============================================================
# 主流程
# ============================================================
main() {
    echo "======================================"
    echo "  Deco D2C 一键转码"
    echo "======================================"
    echo "  平台: $OS_TYPE | $(uname -m)"
    echo ""

    parse_args "$@"

    step_env_check
    echo ""
    step_validate_url
    echo ""
    step_detect_dsl
    echo ""
    step_detect_mode
    echo ""
    step_convert

    echo ""
    echo "======================================"
    ok "全部完成！"
    echo "======================================"
    echo ""
    echo "下一步: 运行产物集成脚本"
    echo "  bash $SCRIPT_DIR/deco-integrate.sh"

    # 显式返回 0，确保转码成功时退出码正确
    exit 0
}

main "$@"
