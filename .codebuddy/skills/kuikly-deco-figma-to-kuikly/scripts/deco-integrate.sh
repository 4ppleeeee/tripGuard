#!/usr/bin/env bash
# ============================================================
# deco-integrate.sh — Deco D2C 产物集成到 Kuikly 项目（跨平台通用版）
# 用法: bash deco-integrate.sh [产物目录] [--project-dir <项目根目录>] [--pages-dir <目标目录>]
#
# 参数:
#   [产物目录]                   转码产物路径（可选，默认自动查找最新）
#   --project-dir <path>        Kuikly 项目根目录（默认当前目录）
#   --pages-dir <path>          页面目标目录（相对于项目根目录，默认自动探测）
#   --dry-run                   仅预览，不实际复制
#
# 支持平台: macOS / Linux / Windows (WSL/Git Bash)
# 返回: 0 = 集成成功, 非 0 = 失败
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

# 跨平台 sed -i
portable_sed_i() {
    local expression="$1"
    local file="$2"
    if [ "$OS_TYPE" = "macos" ]; then
        sed -i '' "$expression" "$file"
    else
        sed -i "$expression" "$file"
    fi
}

# ---- 默认值 ----
ARTIFACT_DIR=""
PROJECT_DIR="$(pwd)"
PAGES_DIR=""         # 空=自动探测
DRY_RUN=false

# ============================================================
# 解析参数
# ============================================================
parse_args() {
    while [ $# -gt 0 ]; do
        case "$1" in
            --project-dir)
                shift
                PROJECT_DIR="${1:-}"
                ;;
            --pages-dir)
                shift
                PAGES_DIR="${1:-}"
                ;;
            --dry-run)
                DRY_RUN=true
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                if [ -z "$ARTIFACT_DIR" ]; then
                    ARTIFACT_DIR="$1"
                else
                    fail "未知参数: $1"
                    usage
                    exit 1
                fi
                ;;
        esac
        shift
    done
}

usage() {
    echo "用法: bash $(basename "$0") [产物目录] [选项]"
    echo ""
    echo "选项:"
    echo "  --project-dir <path>   项目根目录（默认当前目录）"
    echo "  --pages-dir <path>     页面目标目录，相对于项目根目录（默认自动探测）"
    echo "  --dry-run              仅预览，不实际复制"
    echo "  -h, --help             显示帮助"
    echo ""
    echo "页面目录自动探测策略："
    echo "  1. 扫描项目中包含 @Composable 或 attr { 的 .kt 文件"
    echo "  2. 查找名为 pages/、page/、demo/ 的目录"
    echo "  3. 回退到项目中第一个包含 .kt 文件的 kotlin/ 子目录"
}

# ============================================================
# 自动探测页面目录
# ============================================================
auto_detect_pages_dir() {
    info "自动探测页面目录..."

    # 策略 1：查找包含 Kuikly 代码的目录
    local kuikly_dirs=()

    # 找包含 @Composable 或 attr { 的 .kt 文件所在目录
    while IFS= read -r f; do
        local dir
        dir=$(dirname "$f")
        # 取相对于 PROJECT_DIR 的路径
        local rel_dir="${dir#$PROJECT_DIR/}"
        # 避免重复
        local already=false
        for existing in "${kuikly_dirs[@]+"${kuikly_dirs[@]}"}"; do
            if [ "$existing" = "$rel_dir" ]; then
                already=true
                break
            fi
        done
        if ! $already; then
            kuikly_dirs+=("$rel_dir")
        fi
    done < <(grep -rlE "@Composable|attr \{" "$PROJECT_DIR" --include="*.kt" 2>/dev/null | head -20)

    # 在找到的目录中，优先选择名为 pages/demo、pages、page 的
    for pattern in "pages/demo" "pages" "page"; do
        for dir in "${kuikly_dirs[@]+"${kuikly_dirs[@]}"}"; do
            if [[ "$dir" == *"$pattern"* ]]; then
                PAGES_DIR="$dir"
                ok "自动探测到页面目录: $PAGES_DIR"
                return 0
            fi
        done
    done

    # 策略 2：查找项目中名为 pages 或 page 的目录
    local pages_found
    pages_found=$(find "$PROJECT_DIR" -type d \( -name "pages" -o -name "page" \) 2>/dev/null | head -1)
    if [ -n "$pages_found" ]; then
        PAGES_DIR="${pages_found#$PROJECT_DIR/}"
        ok "自动探测到页面目录: $PAGES_DIR"
        return 0
    fi

    # 策略 3：回退到包含 .kt 文件的 kotlin/ 子目录
    local kotlin_dir
    kotlin_dir=$(find "$PROJECT_DIR" -type d -name "kotlin" 2>/dev/null | head -1)
    if [ -n "$kotlin_dir" ]; then
        # 在 kotlin/ 下找最深的包含 .kt 的目录
        local kt_dir
        kt_dir=$(find "$kotlin_dir" -name "*.kt" -type f 2>/dev/null | head -1)
        if [ -n "$kt_dir" ]; then
            PAGES_DIR="$(dirname "$kt_dir")"
            PAGES_DIR="${PAGES_DIR#$PROJECT_DIR/}"
            ok "使用回退目录: $PAGES_DIR"
            return 0
        fi
    fi

    # 无法自动探测
    warn "无法自动探测页面目录"
    echo "  请使用 --pages-dir 参数指定目标目录"
    echo "  例如: bash $(basename "$0") --pages-dir src/commonMain/kotlin/com/example/pages/"
    return 1
}

# ============================================================
# 探测 deco output 目录（通用多路径策略）
# ============================================================
find_deco_output_base() {
    local candidates=()

    # 1. 从 npm root -g 推断
    local npm_root
    npm_root=$(npm root -g 2>/dev/null || echo "")
    if [ -n "$npm_root" ]; then
        candidates+=("$npm_root/@tencent/deco/output")
    fi

    # 2. Volta 全局包路径
    if [ -d "$HOME/.volta" ]; then
        candidates+=("$HOME/.volta/tools/image/packages/@tencent/deco/output")
    fi

    # 3. nvm 路径
    local node_ver
    node_ver=$(node -v 2>/dev/null || echo "")
    if [ -n "$node_ver" ] && [ -d "$HOME/.nvm" ]; then
        candidates+=("$HOME/.nvm/versions/node/$node_ver/lib/node_modules/@tencent/deco/output")
    fi

    # 4. 常见系统路径（按平台）
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

    for base in "${candidates[@]}"; do
        if [ -d "$base" ]; then
            echo "$base"
            return 0
        fi
    done
    return 1
}

# ============================================================
# 查找产物目录
# ============================================================
find_artifact_dir() {
    info "查找转码产物..."

    if [ -n "$ARTIFACT_DIR" ] && [ -d "$ARTIFACT_DIR" ]; then
        ok "使用指定产物目录: $ARTIFACT_DIR"
        return 0
    fi

    # 从 deco-convert.sh 的缓存读取
    local cache_files=(
        "/tmp/deco-latest-output-$$"
        "/tmp/deco-latest-output"
    )
    for cache in "${cache_files[@]}"; do
        if [ -f "$cache" ]; then
            ARTIFACT_DIR=$(cat "$cache")
            if [ -d "$ARTIFACT_DIR" ]; then
                ok "使用最近转码产物: $ARTIFACT_DIR"
                return 0
            fi
        fi
    done

    # 自动查找最新目录
    set +e
    local output_base
    output_base=$(find_deco_output_base)
    set -e

    if [ -n "$output_base" ]; then
        ARTIFACT_DIR=$(ls -td "$output_base"/*/ 2>/dev/null | head -1)
        if [ -n "$ARTIFACT_DIR" ] && [ -d "$ARTIFACT_DIR" ]; then
            ok "自动查找最新产物: $ARTIFACT_DIR"
            return 0
        fi
    fi

    fail "未找到任何转码产物"
    echo "  请指定产物目录: bash $(basename "$0") /path/to/artifact"
    return 1
}

# ============================================================
# 列出产物内容
# ============================================================
list_artifacts() {
    info "产物内容:"
    echo ""

    local kt_files=()
    local asset_files=()

    # 查找 .kt 文件
    while IFS= read -r f; do
        kt_files+=("$f")
    done < <(find "$ARTIFACT_DIR" -maxdepth 1 -name "*.kt" -type f 2>/dev/null)

    # 查找资源文件
    if [ -d "$ARTIFACT_DIR/assets" ]; then
        while IFS= read -r f; do
            asset_files+=("$f")
        done < <(find "$ARTIFACT_DIR/assets" -type f 2>/dev/null)
    fi

    if [ ${#kt_files[@]} -eq 0 ]; then
        fail "未找到 .kt 文件"
        return 1
    fi

    echo "  Kotlin 文件 (${#kt_files[@]}):"
    for f in "${kt_files[@]}"; do
        echo "    $(basename "$f")"
    done

    if [ ${#asset_files[@]} -gt 0 ]; then
        echo ""
        echo "  图片资源 (${#asset_files[@]}):"
        for f in "${asset_files[@]}"; do
            echo "    $(basename "$f")"
        done
    else
        echo ""
        echo "  图片资源: 无"
    fi

    echo ""
}

# ============================================================
# 复制 .kt 文件（自动重命名 + 修复 package）
# ============================================================
copy_kt_files() {
    local target_dir="$PROJECT_DIR/$PAGES_DIR"

    info "复制 Kotlin 文件到: $target_dir"

    if [ ! -d "$target_dir" ]; then
        if $DRY_RUN; then
            warn "[dry-run] 将创建目录: $target_dir"
        else
            mkdir -p "$target_dir"
            ok "创建目录: $target_dir"
        fi
    fi

    # 推断目标 package（从目标目录路径中提取）
    local target_package=""
    # 尝试提取 kotlin/ 之后的路径部分
    if [[ "$target_dir" == *"/kotlin/"* ]]; then
        target_package=$(echo "$target_dir" | sed 's|.*/kotlin/||' | tr '/' '.')
    # 也支持 java/ 目录结构
    elif [[ "$target_dir" == *"/java/"* ]]; then
        target_package=$(echo "$target_dir" | sed 's|.*/java/||' | tr '/' '.')
    fi

    local count=0
    while IFS= read -r f; do
        local orig_name
        orig_name=$(basename "$f")

        # 去掉日期后缀：FileName_2026-03-16_12-43.kt → FileName.kt
        local clean_name
        clean_name=$(echo "$orig_name" | sed -E 's/_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}(\.kt)$/\1/')

        if $DRY_RUN; then
            if [ "$orig_name" != "$clean_name" ]; then
                echo "  [dry-run] $orig_name → $target_dir/$clean_name (重命名)"
            else
                echo "  [dry-run] $orig_name → $target_dir/"
            fi
            if [ -n "$target_package" ]; then
                local orig_pkg
                orig_pkg=$(grep -m1 "^package " "$f" 2>/dev/null | awk '{print $2}' || true)
                if [ -n "$orig_pkg" ] && [ "$orig_pkg" != "$target_package" ]; then
                    echo "  [dry-run]   package: $orig_pkg → $target_package"
                fi
            fi
        else
            cp "$f" "$target_dir/$clean_name"
            local target_file="$target_dir/$clean_name"

            # 修复 package 声明（跨平台 sed -i）
            if [ -n "$target_package" ]; then
                local orig_package
                orig_package=$(grep -m1 "^package " "$target_file" 2>/dev/null | awk '{print $2}' || true)
                if [ -n "$orig_package" ] && [ "$orig_package" != "$target_package" ]; then
                    portable_sed_i "s|^package ${orig_package}|package ${target_package}|" "$target_file" 2>/dev/null || true
                    echo "  → $clean_name (package: $orig_package → $target_package)"
                else
                    echo "  → $clean_name"
                fi
            else
                if [ "$orig_name" != "$clean_name" ]; then
                    echo "  → $clean_name (原名: $orig_name)"
                else
                    echo "  → $clean_name"
                fi
            fi
        fi
        count=$((count + 1))
    done < <(find "$ARTIFACT_DIR" -maxdepth 1 -name "*.kt" -type f 2>/dev/null)

    ok "已复制 ${count} 个 Kotlin 文件"
}

# ============================================================
# 复制图片资源
# ============================================================
copy_assets() {
    if [ ! -d "$ARTIFACT_DIR/assets" ]; then
        info "无图片资源需要复制"
        return 0
    fi

    local asset_count
    asset_count=$(find "$ARTIFACT_DIR/assets" -type f 2>/dev/null | wc -l | tr -d ' ')

    if [ "$asset_count" -eq 0 ]; then
        info "assets 目录为空，跳过"
        return 0
    fi

    info "图片资源 (${asset_count} 个文件)"

    # 检查 assets-manifest.json 中是否有 COS URL
    if [ -f "$ARTIFACT_DIR/assets-manifest.json" ]; then
        echo ""
        warn "发现 assets-manifest.json — 图片可能已上传到 CDN/COS"
        echo "  如果代码中引用的是网络 URL，则无需复制本地资源"
        echo "  如果需要本地资源，请手动复制到项目的资源目录"
    fi

    # 列出资源文件供参考
    echo ""
    echo "  资源文件列表:"
    find "$ARTIFACT_DIR/assets" -type f 2>/dev/null | while read -r f; do
        echo "    $(basename "$f")"
    done

    ok "请根据项目配置手动集成图片资源"
}

# ============================================================
# 提示路由注册
# ============================================================
show_route_reminder() {
    echo ""
    info "路由注册提醒"
    echo ""

    # 列出新增的页面
    info "新增的页面文件:"
    while IFS= read -r f; do
        local basename_f
        basename_f=$(basename "$f")
        # 去掉文件名中的日期后缀用于显示
        local display_name
        display_name=$(echo "$basename_f" | sed -E 's/_[0-9]{4}-[0-9]{2}-[0-9]{2}_[0-9]{2}-[0-9]{2}(\.kt)$/\1/')

        # 检测 @Page 注解（Compose DSL 自动注册路由）
        # 注意：grep 无匹配返回 1，在 pipefail 下会导致退出，必须用 || true 防护
        local page_name
        page_name=$(grep -o '@Page("[^"]*")' "$f" 2>/dev/null | sed 's/@Page("//;s/")//' | head -1 || true)
        if [ -n "$page_name" ]; then
            echo "  → $display_name — 路由: \"$page_name\" (@Page 自动注册 ✅)"
        else
            # 传统 DSL：查找 class 声明
            local classname
            classname=$(grep -o "class [A-Za-z0-9_]*" "$f" 2>/dev/null | head -1 | awk '{print $2}' || true)
            if [ -n "$classname" ]; then
                echo "  → $display_name — 类名: $classname (需在路由配置中手动注册)"
            else
                echo "  → $display_name"
            fi
        fi
    done < <(find "$ARTIFACT_DIR" -maxdepth 1 -name "*.kt" -type f 2>/dev/null)

    echo ""
    echo "  提示: 如果生成代码包含 @Page 注解，页面会自动注册到路由。"
    echo "  否则请在项目的路由配置文件中手动添加导航入口。"
}

# ============================================================
# 主流程
# ============================================================
main() {
    echo "======================================"
    echo "  Deco D2C 产物集成"
    echo "======================================"
    echo ""

    parse_args "$@"

    if $DRY_RUN; then
        warn "DRY-RUN 模式 — 仅预览，不实际操作"
        echo ""
    fi

    find_artifact_dir || exit 1
    echo ""

    # 自动探测或验证页面目录
    if [ -z "$PAGES_DIR" ]; then
        auto_detect_pages_dir || exit 1
    else
        ok "使用指定页面目录: $PAGES_DIR"
    fi
    echo ""

    list_artifacts || exit 1
    copy_kt_files
    echo ""
    copy_assets
    show_route_reminder

    echo ""
    echo "======================================"
    if $DRY_RUN; then
        ok "预览完成（未执行实际操作）"
    else
        ok "产物集成完成 ✅"
    fi
    echo "======================================"
}

main "$@"
