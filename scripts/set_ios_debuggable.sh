#!/bin/bash

# WeSeeCore iOS项目自动化设置脚本

set -e  # 遇到错误立即退出

# 配置键名
WESEE_IOS_PATH_KEY="wesee.ios.path"

# 全局变量
IOS_TARGET=""
IOS_TARGET_TYPE=""
IOS_TARGET_LABEL=""
ACTION=""
TARGET_PLATFORM="ios"
SHOULD_MODIFY_PODFILE=false
SHOULD_RESTORE_FROM_BACKUP=false
SHOULD_RESTORE_PODFILES=false
PODFILE_RESTORE_RESULT="none"
GIT_AVAILABLE=false
GIT_IOSAPP_TRACKED=false
GIT_SKIP_SET=false

# 日志函数
log() {
    local level="$1"
    shift
    echo "[$level] $*"
}

log_info() { log INFO "$@"; }
log_success() { log SUCCESS "$@"; }
log_warning() { log WARNING "$@"; }
log_error() { log ERROR "$@"; }

print_step() {
    echo ""
    echo "--- $1 ---"
}

# 从 local.properties 读取配置值
get_property_value() {
    local key="$1"
    local properties_file="local.properties"

    if [ -f "${properties_file}" ]; then
        grep "^${key}=" "${properties_file}" 2>/dev/null | cut -d'=' -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//'
    fi
}

# 设置 local.properties 中的配置值
set_property_value() {
    local key="$1"
    local value="$2"
    local properties_file="local.properties"

    if [ ! -f "${properties_file}" ]; then
        touch "${properties_file}"
    fi

    if grep -q "^${key}=" "${properties_file}"; then
        # 更新现有配置
        sed -i.tmp "s|^${key}=.*|${key}=${value}|" "${properties_file}"
        rm "${properties_file}.tmp"
    else
        # 添加新配置
        echo "${key}=${value}" >> "${properties_file}"
    fi
}

# 初始化 git 环境信息
init_git_environment() {
    if command -v git &> /dev/null && git rev-parse --is-inside-work-tree &> /dev/null; then
        GIT_AVAILABLE=true
        if [ -n "$(git ls-files iosApp)" ]; then
            GIT_IOSAPP_TRACKED=true
            log_info "检测到 git 正在追踪 iosApp 目录，脚本将自动处理 skip-worktree 设置"
        else
            log_info "git 仓库中未追踪 iosApp 目录，无需额外处理"
        fi
    else
        log_warning "未检测到 git 仓库，跳过 git 相关操作"
    fi
}

# 设置 git 对 iosApp 目录的 skip-worktree 属性，避免产生差异
set_iosapp_skip_worktree() {
    if [ "${GIT_AVAILABLE}" != "true" ] || [ "${GIT_IOSAPP_TRACKED}" != "true" ]; then
        return
    fi

    if git ls-files -v iosApp | grep -q '^S'; then
        GIT_SKIP_SET=true
        log_info "git 已忽略 iosApp 目录的变更"
        return
    fi

    local tracked_files
    tracked_files=$(git ls-files iosApp)
    if [ -z "${tracked_files}" ]; then
        return
    fi

    git ls-files -z iosApp | xargs -0 git update-index --skip-worktree
    GIT_SKIP_SET=true
    log_success "git 已设置 skip-worktree 忽略 iosApp 目录的变更"
}

# 清除 skip-worktree 属性，恢复 git 对 iosApp 目录的追踪
clear_iosapp_skip_worktree() {
    if [ "${GIT_AVAILABLE}" != "true" ] || [ "${GIT_IOSAPP_TRACKED}" != "true" ]; then
        GIT_SKIP_SET=false
        return
    fi

    if ! git ls-files -v iosApp | grep -q '^S'; then
        GIT_SKIP_SET=false
        return
    fi

    local tracked_files
    tracked_files=$(git ls-files iosApp)
    if [ -z "${tracked_files}" ]; then
        GIT_SKIP_SET=false
        return
    fi

    git ls-files -z iosApp | xargs -0 git update-index --no-skip-worktree
    GIT_SKIP_SET=false
    log_success "git 已恢复对 iosApp 目录的追踪"
}

# 根据当前链接的目标处理 git 状态
handle_git_status_after_link() {
    if [ "${IOS_TARGET_TYPE}" = "external" ]; then
        set_iosapp_skip_worktree
    else
        clear_iosapp_skip_worktree
    fi
}

# 备份 Podfile 以便恢复
backup_podfile() {
    local podfile="$1"
    if [ ! -f "${podfile}" ]; then
        return
    fi

    local backup="${podfile}.qn_backup"
    if [ -f "${backup}" ]; then
        return
    fi

    if cp "${podfile}" "${backup}"; then
        log_info "已备份 Podfile: ${backup}"
    else
        log_warning "Podfile 备份失败: ${podfile}"
    fi
}

# 恢复单个 Podfile
restore_podfile() {
    local podfile="$1"

    if [ ! -f "${podfile}" ]; then
        return 1
    fi

    local backup="${podfile}.qn_backup"

    if [ -f "${backup}" ]; then
        if cp "${backup}" "${podfile}"; then
            rm -f "${backup}"
            log_success "Podfile 已从备份恢复: ${podfile}"
            return 0
        else
            log_warning "Podfile 备份恢复失败: ${podfile}"
            return 1
        fi
    fi

    if command -v git &> /dev/null; then
        if (cd "$(dirname "${podfile}")" && git rev-parse --is-inside-work-tree &> /dev/null); then
            if (cd "$(dirname "${podfile}")" && git checkout -- "$(basename "${podfile}")" &> /dev/null); then
                log_success "Podfile 已通过 git 恢复: ${podfile}"
                return 0
            fi
        fi
    fi

    log_info "未找到 Podfile 备份且无法通过 git 恢复: ${podfile}"
    return 1
}

# 批量恢复可能被修改的 Podfile
restore_modified_podfiles() {
    if [ "${SHOULD_RESTORE_PODFILES}" != true ]; then
        PODFILE_RESTORE_RESULT="skipped"
        return
    fi

    local restored=false
    local candidates=("iosAppDemo/Podfile")

    if [ -n "${WESEE_IOS_PATH}" ]; then
        candidates+=("${WESEE_IOS_PATH}/Podfile")
    fi

    for podfile in "${candidates[@]}"; do
        if restore_podfile "${podfile}"; then
            restored=true
        fi
    done

    if [ "${restored}" = true ]; then
        PODFILE_RESTORE_RESULT="restored"
        log_success "Podfile 已恢复为原始状态"
    else
        PODFILE_RESTORE_RESULT="not_found"
        log_info "未找到需要恢复的 Podfile"
    fi

    echo ""
}

# 检查WeSee_iOS路径
check_wesee_ios_path() {
    local properties_file="local.properties"

    # 从 local.properties 读取路径
    WESEE_IOS_PATH=$(get_property_value "${WESEE_IOS_PATH_KEY}")

    if [ -z "${WESEE_IOS_PATH}" ]; then
        log_warning "在 ${properties_file} 中未找到 ${WESEE_IOS_PATH_KEY} 配置"
        echo ""
        echo "请输入 WeSee_iOS 项目的完整路径："

        while true; do
            read -p "WeSee_iOS 路径: " input_path

            # 检查用户输入是否为空
            if [ -z "${input_path}" ]; then
                log_warning "路径不能为空，请重新输入"
                continue
            fi

            # 展开波浪号
            input_path="${input_path/#\~/$HOME}"

            # 验证路径是否存在
            if [ -d "${input_path}" ]; then
                WESEE_IOS_PATH="${input_path}"
                log_info "保存路径到 ${properties_file}: ${WESEE_IOS_PATH_KEY}=${WESEE_IOS_PATH}"
                set_property_value "${WESEE_IOS_PATH_KEY}" "${WESEE_IOS_PATH}"
                log_success "WeSee_iOS 路径已保存并验证通过"
                break
            else
                log_error "路径不存在: ${input_path}"
                echo "请输入有效的目录路径"
            fi
        done
    else
        log_info "从 ${properties_file} 读取到 WeSee_iOS 路径: ${WESEE_IOS_PATH}"

        # 验证路径是否存在
        if [ ! -d "${WESEE_IOS_PATH}" ]; then
            log_error "配置的 WeSee_iOS 路径不存在: ${WESEE_IOS_PATH}"
            log_error "请检查 ${properties_file} 中的 ${WESEE_IOS_PATH_KEY} 配置"
            echo ""
            echo "是否要重新设置路径? (y/n)"
            read -p "请选择: " reset_choice

            if [[ "${reset_choice}" =~ ^[Yy]$ ]]; then
                # 删除旧配置并重新设置
                if [ -f "${properties_file}" ]; then
                    sed -i.tmp "/^${WESEE_IOS_PATH_KEY}=/d" "${properties_file}"
                    rm "${properties_file}.tmp"
                fi
                # 递归调用自己重新设置路径
                check_wesee_ios_path
                return
            else
                exit 1
            fi
        else
            log_success "WeSee_iOS 路径验证通过"
        fi
    fi
}

# 检查必要的工具
check_prerequisites() {
    log_info "检查必要工具和配置..."

    # 检查Android Studio是否安装
    if ! command -v "/Applications/Android Studio.app/Contents/MacOS/studio" &> /dev/null; then
        log_error "Android Studio 未找到，请确保已正确安装"
        exit 1
    fi

    log_success "所有必要工具检查完成"
}

# 步骤1: 备份现有的iosApp到iosAppDemo
backup_iosapp_to_demo() {
    print_step "备份现有的 iosApp"

    local source_dir="iosApp"
    local backup_dir="iosAppDemo"

    if [ ! -d "${source_dir}" ]; then
        log_info "未找到原始 iosApp 目录，跳过备份"
        echo ""
        return
    fi

    if [ ! -d "${backup_dir}" ]; then
        cp -R "${source_dir}" "${backup_dir}"
        log_success "已将 iosApp 备份到 ${backup_dir}"
    else
        log_info "检测到 ${backup_dir} 已存在，跳过备份"
    fi

    if [ -L "${source_dir}" ]; then
        rm "${source_dir}"
        log_info "已移除旧的 iosApp 符号链接"
    fi

    rm -rf "${source_dir}"
    log_info "已移除原始 iosApp 目录，后续将创建符号链接"
    echo ""
}

# 还原 iosApp 目录到备份版本
restore_iosapp_from_backup() {
    print_step "还原原始 iosApp 目录"

    local backup_dir="iosAppDemo"
    local target_dir="iosApp"

    if [ ! -d "${backup_dir}" ]; then
        log_error "未找到备份目录 ${backup_dir}，无法还原"
        exit 1
    fi

    rm -rf "${target_dir}"
    cp -R "${backup_dir}" "${target_dir}"
    log_success "已从 ${backup_dir} 还原 iosApp"

    IOS_TARGET="${target_dir}"
    IOS_TARGET_TYPE="internal"
    clear_iosapp_skip_worktree
    echo ""
}

# 步骤2: 选择执行的操作
choose_operation() {
    print_step "选择操作"

    ACTION=""
    TARGET_PLATFORM="ios"
    IOS_TARGET=""
    IOS_TARGET_TYPE=""
    IOS_TARGET_LABEL=""
    PODFILE_RESTORE_RESULT="none"

    echo "1. 使用 local.properties 中的 WeSee_iOS 项目 (${WESEE_IOS_PATH})"
    echo "2. 一键还原安卓 (恢复 iosApp 并切换到 Android 平台)"
    echo "3. 一键还原 iOS (恢复 iosApp 原始目录)"
    echo ""

    while true; do
        read -p "请输入选择 (1-3): " choice
        case "${choice}" in
            1)
                if [ -z "${WESEE_IOS_PATH}" ]; then
                    log_error "未检测到 WeSee_iOS 路径配置，请检查 local.properties"
                    exit 1
                fi
                if [ ! -d "${WESEE_IOS_PATH}" ]; then
                    log_error "目录 ${WESEE_IOS_PATH} 不存在，请检查 ${WESEE_IOS_PATH_KEY} 配置"
                    exit 1
                fi
                ACTION="link_external"
                IOS_TARGET="${WESEE_IOS_PATH}"
                IOS_TARGET_TYPE="external"
                IOS_TARGET_LABEL="${WESEE_IOS_PATH}"
                TARGET_PLATFORM="ios"
                break
                ;;
            2)
                ACTION="restore_android"
                IOS_TARGET="iosApp"
                IOS_TARGET_TYPE="internal"
                IOS_TARGET_LABEL="iosApp (restored from iosAppDemo)"
                TARGET_PLATFORM="android"
                break
                ;;
            3)
                ACTION="restore_ios"
                IOS_TARGET="iosApp"
                IOS_TARGET_TYPE="internal"
                IOS_TARGET_LABEL="iosApp (restored from iosAppDemo)"
                TARGET_PLATFORM="ios"
                break
                ;;
            *)
                log_warning "无效选择，请输入 1-3"
                ;;
        esac
    done
}

set_action_flags() {
    SHOULD_MODIFY_PODFILE=false
    SHOULD_RESTORE_FROM_BACKUP=false
    SHOULD_RESTORE_PODFILES=false
    PODFILE_RESTORE_RESULT="none"

    case "${ACTION}" in
        link_external)
            SHOULD_MODIFY_PODFILE=true
            ;;
        restore_android|restore_ios)
            SHOULD_RESTORE_FROM_BACKUP=true
            SHOULD_RESTORE_PODFILES=true
            ;;
    esac
}

link_ios_target() {
    print_step "更新 iosApp 符号链接"

    if [ -z "${IOS_TARGET}" ]; then
        log_error "未指定符号链接目标"
        exit 1
    fi

    if [ -L "iosApp" ] || [ -d "iosApp" ]; then
        rm -rf "iosApp"
    fi

    ln -s "${IOS_TARGET}" "iosApp"
    log_success "已创建符号链接: iosApp -> ${IOS_TARGET}"

    handle_git_status_after_link
    echo ""
}

# 步骤3: 修改local.properties
modify_local_properties() {
    print_step "更新 local.properties"

    local platform_key="qqnews.kmm.build.platform"
    local platform_value="${TARGET_PLATFORM:-ios}"

    # 检查并设置平台配置
    local current_platform=$(get_property_value "${platform_key}")
    if [ -n "${current_platform}" ]; then
        log_info "检测到已存在 ${platform_key} 配置，更新为 ${platform_value}"
    else
        log_info "在 local.properties 添加 ${platform_key}=${platform_value}"
    fi

    set_property_value "${platform_key}" "${platform_value}"
    log_success "local.properties 修改完成"
    echo ""
}

# 步骤4: 修改目标iOS App的Podfile
modify_podfile() {
    print_step "调整 Podfile (${IOS_TARGET})"

    local podfile="${IOS_TARGET}/Podfile"
    local umbrella_path="$(pwd)/umbrella"

    if [ ! -f "${podfile}" ]; then
        log_error "文件不存在: ${podfile}"
        exit 1
    fi

    backup_podfile "${podfile}"

    log_info "修改 Podfile 中的 umbrella 配置，删除条件判断并设置为本地路径..."

    # 检查是否存在 ENV['_KMMDebug'] 条件块
    if grep -q "if ENV\\['_KMMDebug'\\]" "${podfile}"; then
        log_info "找到 ENV['_KMMDebug'] 条件语句，将整个条件块替换为单行配置..."

        # 使用 awk 来替换整个 if-else-end 块
        awk -v umbrellapath="${umbrella_path}" '
        BEGIN { in_kmm_block = 0; replaced = 0 }
        /if ENV\[\x27_KMMDebug\x27\]/ {
            in_kmm_block = 1
            print "  pod \x27umbrella\x27, :path => \x27" umbrellapath "\x27, :inhibit_warnings => true"
            replaced = 1
            next
        }
        /^[ \t]*end/ && in_kmm_block {
            in_kmm_block = 0
            next
        }
        !in_kmm_block { print }
        ' "${podfile}" > "${podfile}.tmp" && mv "${podfile}.tmp" "${podfile}"

        if [ $? -eq 0 ]; then
            log_success "ENV['_KMMDebug'] 条件块已删除，替换为: pod 'umbrella', :path => '${umbrella_path}', :inhibit_warnings => true"
        else
            log_error "替换失败，请检查 Podfile 格式"
            exit 1
        fi
    else
        log_info "未找到 ENV['_KMMDebug'] 条件语句，检查直接的 umbrella 配置..."

        # 如果没有找到条件语句，检查是否有直接的 umbrella 配置并修改路径
        if grep -q "pod 'umbrella'" "${podfile}"; then
            log_info "找到 umbrella 配置，修改路径..."
            sed -i.tmp "s|pod 'umbrella'.*|pod 'umbrella', :path => '${umbrella_path}', :inhibit_warnings => true|g" "${podfile}"
            rm "${podfile}.tmp"
            log_success "umbrella 配置已更新为: pod 'umbrella', :path => '${umbrella_path}', :inhibit_warnings => true"
        else
            log_warning "未找到任何 umbrella 配置"
        fi
    fi

    log_success "${IOS_TARGET} Podfile 修改完成"
    echo ""
}

# 步骤5: 在Android Studio中执行sync project with gradle files
sync_gradle_project() {
    print_step "同步 Gradle 项目 (需要手动执行)"

    # 检查Android Studio是否正在运行
    local studio_pid=$(pgrep -f "Android Studio" || true)

    if [ -n "${studio_pid}" ]; then
        log_info "检测到Android Studio正在运行 (PID: ${studio_pid})"
    else
        log_info "启动Android Studio..."
        open -a "Android Studio" "$(pwd)"
        log_info "Android Studio 正在启动..."
    fi

    echo ""
    echo "🚨 【必须手动操作】"
    echo "➤ Android Studio: File -> Sync Project with Gradle Files"
    echo ""
}

# 主函数
main() {
    echo "========================================"
    echo "    WeSeeCore iOS项目自动化调试脚本"
    echo "========================================"
    echo ""

    if [ -d "./umbrella" ]; then
        # 当前已经在WeSeeCore根目录
        log_info "检测到当前在WeSeeCore根目录"
    elif [ -d "../umbrella" ]; then
        # 当前在scripts目录，切换到上级目录
        log_info "检测到当前在scripts目录，切换到WeSeeCore根目录"
        cd ..
    else
        log_error "无法找到WeSeeCore根目录，请确保在WeSeeCore目录或WeSeeCore/scripts目录下执行脚本"
        exit 1
    fi

    log_info "当前工作目录: $(pwd)"
    echo ""

    init_git_environment

    # 检查WeSee_iOS路径
    check_wesee_ios_path
    echo ""

    # 检查先决条件
    check_prerequisites
    echo ""

    # 执行各个步骤
    backup_iosapp_to_demo

    choose_operation
    set_action_flags

    if [ "${SHOULD_RESTORE_FROM_BACKUP}" = true ]; then
        restore_iosapp_from_backup
        restore_modified_podfiles
    else
        link_ios_target
    fi

    modify_local_properties

    if [ "${SHOULD_MODIFY_PODFILE}" = true ]; then
        modify_podfile
    fi

    sync_gradle_project

    echo "═══════════════════════════════════════════════════════════════════════════════"
    log_success "脚本执行完成！"
    echo ""
    echo "📊 执行摘要"
    echo "═══════════════════════════════════════════════════════════════════════════════"
    echo "1. ✅ 原有iosApp已备份到iosAppDemo目录"
    echo "2. ✅ 当前目标: ${IOS_TARGET_LABEL}"
    if [ "${SHOULD_RESTORE_FROM_BACKUP}" = true ]; then
        echo "3. ✅ iosApp 目录已恢复为备份版本"
    else
        echo "3. ✅ iosApp符号链接已创建: iosApp -> ${IOS_TARGET}"
    fi
    echo "4. ✅ local.properties已配置 qqnews.kmm.build.platform=${TARGET_PLATFORM}"
    if [ "${SHOULD_RESTORE_PODFILES}" = true ]; then
        case "${PODFILE_RESTORE_RESULT}" in
            restored)
                echo "5. ✅ 目标 Podfile 已恢复为原始状态"
                ;;
            not_found)
                echo "5. ⚠️ 未找到可恢复的 Podfile (如有需要请手动检查备份或 git)"
                ;;
            *)
                echo "5. ✅ 目标 Podfile 保持原始状态"
                ;;
        esac
    elif [ "${SHOULD_MODIFY_PODFILE}" = true ]; then
        echo "5. ✅ 目标 Podfile 已删除条件判断，umbrella配置为本地路径 (${IOS_TARGET_LABEL})"
    else
        echo "5. ✅ 目标 Podfile 保持原始状态"
    fi
    echo "6. 🚨 请手动在Android Studio中同步Gradle并调试"
    if [ "${GIT_AVAILABLE}" = "true" ] && [ "${GIT_IOSAPP_TRACKED}" = "true" ]; then
        if [ "${GIT_SKIP_SET}" = "true" ]; then
            echo "✅ git 已设置 skip-worktree 忽略 iosApp 目录的变更"
        else
            echo "✅ git 正常追踪 iosApp 目录"
        fi
    fi
    echo "═══════════════════════════════════════════════════════════════════════════════"
    echo ""
    echo "提示：如需修改 WeSee_iOS 路径，请编辑 local.properties 中的 ${WESEE_IOS_PATH_KEY} 配置"
    echo "可用操作：1) ${WESEE_IOS_PATH} (local.properties 配置)  2) 一键还原安卓  3) 一键还原 iOS"
    if [ -L "iosApp" ]; then
        echo "当前符号链接：iosApp -> ${IOS_TARGET} (可重新运行脚本切换操作)"
    else
        echo "当前状态：iosApp 为独立目录 (可重新运行脚本切换操作)"
    fi
}

# 脚本入口点
main "$@"
