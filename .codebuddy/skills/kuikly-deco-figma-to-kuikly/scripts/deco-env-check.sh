#!/usr/bin/env bash
# ============================================================
# deco-env-check.sh — Deco D2C 转码环境检查（跨平台通用版）
# 用法: bash deco-env-check.sh [--registry <npm-registry-url>]
# 返回: 0 = 环境就绪, 非 0 = 缺少依赖
#
# 支持平台: macOS / Linux / Windows (WSL/Git Bash)
# ============================================================
set -euo pipefail

# ---- 颜色（检测终端是否支持） ----
if [ -t 1 ] && command -v tput &>/dev/null && [ "$(tput colors 2>/dev/null || echo 0)" -ge 8 ]; then
    RED='\033[0;31m'
    GREEN='\033[0;32m'
    YELLOW='\033[1;33m'
    NC='\033[0m'
else
    RED='' GREEN='' YELLOW='' NC=''
fi

ok()   { echo -e "${GREEN}✔${NC} $*"; }
warn() { echo -e "${YELLOW}⚠${NC} $*"; }
fail() { echo -e "${RED}✗${NC} $*"; }

# ---- 全局变量 ----
MIN_NODE_MAJOR=18
DECO_PKG="@tencent/deco"
DECO_REGISTRY=""        # 空=使用默认（腾讯内网镜像源）
LAST_UPDATE_FILE="$HOME/.deco/.last_update"
EXIT_CODE=0
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ============================================================
# Beacon 事件上报（后台异步，不影响主流程）
# ============================================================
beacon_report() {
    local node_script="$SCRIPT_DIR/beacon-report.js"
    local skill_name="deco-figma-to-kuikly"
    local project_dir="$(pwd)"
    local project_name=$(basename "$project_dir")

    # 检查并安装依赖
    if [ ! -d "$SCRIPT_DIR/node_modules/@tencent/beacon-node-new-sdk" ]; then
        (cd "$SCRIPT_DIR" && npm config set registry https://mirrors.tencent.com/npm/ 2>/dev/null && npm install @tencent/beacon-node-new-sdk --silent 2>/dev/null)
    fi

    # 后台执行上报，不阻塞主流程
    if command -v node &>/dev/null && [ -f "$node_script" ]; then
        node "$node_script" "skill_invoked" "skill_name=$skill_name" "project_name=$project_name" "project_dir=$project_dir" &>/dev/null &
    fi
}

# ---- 默认 registry（腾讯内网镜像源） ----
DEFAULT_REGISTRY="https://mirrors.tencent.com/npm/"

# ---- 解析参数 ----
while [ $# -gt 0 ]; do
    case "$1" in
        --registry)
            shift
            DECO_REGISTRY="${1:-}"
            ;;
    esac
    shift
done

# 构建 npm install 命令（默认使用腾讯内网镜像源，可通过 --registry 覆盖）
npm_install_deco() {
    local registry="${DECO_REGISTRY:-$DEFAULT_REGISTRY}"
    npm install -g "$DECO_PKG" --registry="$registry" 2>&1
}

# ============================================================
# 1. 检查 Node.js >= 18
# ============================================================
check_node() {
    echo ""
    echo "── Node.js ──"

    # 尝试加载 nvm（如果 nvm 存在但未加载）
    if ! command -v node &>/dev/null; then
        # 尝试常见 nvm 路径
        local nvm_candidates=(
            "$HOME/.nvm/nvm.sh"
            "${NVM_DIR:-/dev/null}/nvm.sh"
            "/usr/local/share/nvm/nvm.sh"      # Homebrew nvm (macOS)
            "/usr/share/nvm/nvm.sh"             # Linux 系统包
        )
        for nvm_path in "${nvm_candidates[@]}"; do
            if [ -f "$nvm_path" ]; then
                warn "nvm 存在但未加载，正在加载..."
                # shellcheck source=/dev/null
                source "$nvm_path" 2>/dev/null || true
                break
            fi
        done
    fi

    if command -v node &>/dev/null; then
        local ver
        ver=$(node -v 2>/dev/null | sed 's/^v//')
        local major
        major=$(echo "$ver" | cut -d. -f1)
        if [ "$major" -ge "$MIN_NODE_MAJOR" ] 2>/dev/null; then
            ok "Node.js v${ver} (>= ${MIN_NODE_MAJOR})"
            return 0
        else
            warn "Node.js v${ver} 版本过低（需要 >= ${MIN_NODE_MAJOR}）"
        fi
    else
        warn "Node.js 未安装"
    fi

    # 尝试自动安装
    if command -v nvm &>/dev/null; then
        echo "  → 通过 nvm 安装 Node.js ${MIN_NODE_MAJOR}..."
        if nvm install "$MIN_NODE_MAJOR" && nvm use "$MIN_NODE_MAJOR"; then
            ok "Node.js $(node -v) 安装成功 (nvm)"
            return 0
        fi
    elif command -v volta &>/dev/null; then
        echo "  → 通过 volta 安装 Node.js ${MIN_NODE_MAJOR}..."
        if volta install "node@${MIN_NODE_MAJOR}"; then
            ok "Node.js $(node -v) 安装成功 (volta)"
            return 0
        fi
    elif command -v fnm &>/dev/null; then
        echo "  → 通过 fnm 安装 Node.js ${MIN_NODE_MAJOR}..."
        if fnm install "$MIN_NODE_MAJOR" && fnm use "$MIN_NODE_MAJOR"; then
            ok "Node.js $(node -v) 安装成功 (fnm)"
            return 0
        fi
    fi

    fail "无法自动安装 Node.js >= ${MIN_NODE_MAJOR}"
    echo "  推荐安装方式："
    echo "    nvm:   https://github.com/nvm-sh/nvm#installing-and-updating"
    echo "    volta: https://volta.sh"
    echo "    fnm:   https://github.com/Schniz/fnm"
    echo "    官网:  https://nodejs.org"
    return 1
}

# ============================================================
# 2. 检查 npm
# ============================================================
check_npm() {
    echo ""
    echo "── npm ──"
    if command -v npm &>/dev/null; then
        ok "npm $(npm -v)"
        return 0
    else
        fail "npm 未找到（npm 随 Node.js 一起安装）"
        return 1
    fi
}

# ============================================================
# 3. 检查 Deco CLI（安装/每日更新）
# ============================================================
check_deco() {
    echo ""
    echo "── Deco CLI ──"

    if command -v deco &>/dev/null; then
        local ver
        ver=$(deco --version 2>/dev/null || echo "unknown")
        ok "Deco CLI ${ver}"

        # 每日更新控制
        local today
        today=$(date +%Y-%m-%d)
        local last_update
        last_update=$(cat "$LAST_UPDATE_FILE" 2>/dev/null || echo "")

        if [ "$last_update" = "$today" ]; then
            ok "今日已更新，跳过"
        else
            echo "  → 检查 Deco CLI 更新..."
            if npm_install_deco; then
                mkdir -p "$(dirname "$LAST_UPDATE_FILE")"
                echo "$today" > "$LAST_UPDATE_FILE"
                ok "Deco CLI 更新成功"
            else
                warn "Deco CLI 更新失败（当前版本仍可使用）"
            fi
        fi
        return 0
    else
        echo "  → 首次安装 Deco CLI..."
        if npm_install_deco; then
            ok "Deco CLI $(deco --version 2>/dev/null) 安装成功"
            return 0
        else
            fail "Deco CLI 安装失败"
            local registry="${DECO_REGISTRY:-$DEFAULT_REGISTRY}"
            echo "  请手动执行: npm install -g $DECO_PKG --registry=$registry"
            echo "  如果需要指定其他 npm 源: bash $(basename "$0") --registry <registry-url>"
            return 1
        fi
    fi
}

# ============================================================
# 主流程
# ============================================================
main() {
    # Beacon 事件上报（后台异步）
    beacon_report

    echo "=============================="
    echo "  Deco D2C 环境检查"
    echo "=============================="

    check_node || EXIT_CODE=1
    if [ "$EXIT_CODE" -ne 0 ]; then
        echo ""
        fail "环境检查未通过：Node.js 缺失"
        exit $EXIT_CODE
    fi

    check_npm || EXIT_CODE=1
    if [ "$EXIT_CODE" -ne 0 ]; then
        echo ""
        fail "环境检查未通过：npm 缺失"
        exit $EXIT_CODE
    fi

    check_deco || EXIT_CODE=1
    if [ "$EXIT_CODE" -ne 0 ]; then
        echo ""
        fail "环境检查未通过：Deco CLI 安装失败"
        exit $EXIT_CODE
    fi

    echo ""
    echo "=============================="
    ok "环境检查全部通过 ✅"
    echo "=============================="
    exit 0
}

main "$@"
