#!/bin/bash
# -*- coding: utf-8 -*-
#
# ViewModel 设计审查小助手 - CLI 工具 (Bash 版)
#
# 功能：
#   1. 接收外部传入的完整 AI 提示词，通过流式 SSE 接口发送给审查小助手
#   2. 实时打印 AI 的回复内容
#   3. 监听返回的生成文件，自动下载到报告目录
#
# 用法：
#   bash vm_review_tool.sh --apikey <API_KEY> --mr-url <MR链接> --question <完整提示词>
#
# 说明：
#   - 所有工程相关配置从同级的 config.json 读取
#   - 脚本不负责生成或补全提示词，`--question` 必须由外部传入完整内容
#   - `--mr-url` 仅作为本次审查关联的 MR 上下文参数，由外部显式传入
#   - 迁移到新工程时只需修改 config.json，无需改动本脚本

set -uo pipefail

# ========== 路径与配置 ==========

# 脚本所在目录（scripts/），取其上一级作为 skill 根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_ROOT="$(dirname "${SCRIPT_DIR}")"
# 项目根目录：优先用 git，fallback 用相对路径（skill 在 .codebuddy/skills/<name>/scripts/）
if PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"; then
    : # 成功获取
else
    PROJECT_ROOT="$(cd "${SKILL_ROOT}/../../../.." && pwd)"
fi
CONFIG_FILE="${SKILL_ROOT}/config.json"

# 从 config.json 读取配置（安全降级：读不到就用默认值）
read_config() {
    local key="$1"
    local default="$2"
    if [[ -f "${CONFIG_FILE}" ]] && command -v python3 &>/dev/null; then
        local val
        val=$(python3 -c "
import json, sys
try:
    with open('${CONFIG_FILE}') as f:
        cfg = json.load(f)
    keys = '${key}'.split('.')
    v = cfg
    for k in keys:
        v = v[k]
    print(v, end='')
except Exception:
    print('${default}', end='')
" 2>/dev/null)
        printf '%s' "${val:-${default}}"
    else
        printf '%s' "${default}"
    fi
}

# 从 config.json 读取报告输出配置
REPORT_OUTPUT_DIR="$(read_config "report.output_dir" "./report")"
# 如果是相对路径，拼接为项目根目录的绝对路径
if [[ "${REPORT_OUTPUT_DIR}" != /* ]]; then
    REPORT_OUTPUT_DIR="${PROJECT_ROOT}/${REPORT_OUTPUT_DIR}"
fi

PROJECT_NAME="$(read_config "project.name" "ViewModel")"

# ========== 可覆盖的服务端常量 ==========
# 通过环境变量 VM_REVIEW_BASE_URL 覆盖
BASE_URL="${VM_REVIEW_BASE_URL:-https://newsai.woa.com}"
STREAM_API="${BASE_URL}/agent-server/api/agent-api/stream"

# ========== 变量 ==========
API_KEY=""
QUESTION=""
MR_URL=""
OUTPUT_DIR="${REPORT_OUTPUT_DIR}"

# ========== 函数 ==========

usage() {
    cat <<EOF
${PROJECT_NAME} ViewModel 设计审查小助手 CLI 工具 (Bash 版)

用法:
  bash vm_review_tool.sh --apikey <API_KEY> --mr-url <MR链接> --question <基础提示词>

必需参数:
  --apikey       API 密钥
  --mr-url       MR 链接
  --question     外部传入的基础 AI 提示词，脚本会在发请求前自动追加 MR 范围约束

可选参数:
  -h, --help     显示此帮助信息

配置文件:
  ${CONFIG_FILE}
  迁移到新工程时只需修改此文件。

环境变量:
  VM_REVIEW_BASE_URL  可选，覆盖默认服务端地址 ${BASE_URL}

示例:
  bash vm_review_tool.sh \
    --apikey "XXXX" \
    --mr-url "https://git.example.com/group/project/-/merge_requests/123" \
    --question "请审查该 MR 的 ViewModel 设计是否符合规范。"
EOF
    exit 0
}

parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --apikey)   API_KEY="$2"; shift 2 ;;
            --mr-url)   MR_URL="$2"; shift 2 ;;
            --question) QUESTION="$2"; shift 2 ;;
            -h|--help)  usage ;;
            *)
                echo "❌ 未知参数: $1"
                echo "使用 --help 查看帮助"
                exit 1
                ;;
        esac
    done

    if [[ -z "${API_KEY}" ]]; then
        echo "❌ 缺少必需参数: --apikey"
        exit 1
    fi
    if [[ -z "${MR_URL}" ]]; then
        echo "❌ 缺少必需参数: --mr-url"
        exit 1
    fi
    if [[ -z "${QUESTION}" ]]; then
        echo "❌ 缺少必需参数: --question"
        exit 1
    fi
}

ensure_dir() {
    local dir="$1"
    if [[ ! -d "${dir}" ]]; then
        mkdir -p "${dir}"
        echo "📁 已创建输出目录: ${dir}"
    fi
}

# 构建并校验安全下载路径（防止路径穿越）
build_safe_output_path() {
    local output_dir="$1"
    local file_name="$2"
    local safe_path

    safe_path=$(python3 - "${output_dir}" "${file_name}" <<'PY'
import os
import re
import sys

output_dir = os.path.realpath(sys.argv[1])
file_name = sys.argv[2]

if not file_name or file_name.startswith(("/", "~")) or "\x00" in file_name:
    raise SystemExit(1)

if not re.fullmatch(r"[A-Za-z0-9._/\-]+", file_name):
    raise SystemExit(1)

norm_name = os.path.normpath(file_name)
if norm_name in ("", ".", "..") or norm_name.startswith("../") or os.path.isabs(norm_name):
    raise SystemExit(1)

candidate = os.path.realpath(os.path.join(output_dir, norm_name))
if candidate != output_dir and not candidate.startswith(output_dir + os.sep):
    raise SystemExit(1)

print(candidate)
PY
) || return 1

    printf "%s" "${safe_path}"
}

download_file() {
    local download_url="$1"
    local file_name="$2"
    local output_dir="$3"

    if [[ "${download_url}" == /* ]]; then
        download_url="${BASE_URL}${download_url}"
    fi

    local save_path
    if ! save_path=$(build_safe_output_path "${output_dir}" "${file_name}"); then
        echo ""
        echo "❌ 非法文件路径，已跳过下载: ${file_name}"
        return 1
    fi

    local file_dir
    file_dir=$(dirname "${save_path}")
    mkdir -p "${file_dir}"

    echo ""
    echo "📥 正在下载文件: ${file_name}"
    echo "   URL: ${download_url}"

    local http_code
    http_code=$(curl -sS -w "%{http_code}" -o "${save_path}" "${download_url}" 2>/dev/null) || true

    if [[ "${http_code}" == "200" ]] && [[ -f "${save_path}" ]]; then
        local file_size
        file_size=$(wc -c < "${save_path}" | tr -d ' ')
        echo "   ✅ 下载完成: ${save_path} (${file_size} bytes)"
        return 0
    else
        echo "   ❌ 下载失败 (HTTP ${http_code})"
        rm -f "${save_path}" 2>/dev/null
        return 1
    fi
}

process_generated_files() {
    local files_json="$1"
    local output_dir="$2"

    ensure_dir "${output_dir}"

    local file_count
    file_count=$(echo "${files_json}" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")

    if [[ "${file_count}" == "0" ]]; then
        echo "ℹ️  没有可下载的文件"
        return
    fi

    local success_count=0
    local fail_count=0

    for i in $(seq 0 $((file_count - 1))); do
        local name download_url is_dir
        name=$(echo "${files_json}" | python3 -c "import sys,json; f=json.load(sys.stdin)[$i]; print(f.get('name','unknown'))" 2>/dev/null)
        download_url=$(echo "${files_json}" | python3 -c "import sys,json; f=json.load(sys.stdin)[$i]; print(f.get('downloadUrl',''))" 2>/dev/null)
        is_dir=$(echo "${files_json}" | python3 -c "import sys,json; f=json.load(sys.stdin)[$i]; print(f.get('isDir',False))" 2>/dev/null)

        if [[ "${is_dir}" == "True" ]]; then
            echo "   ⏭️  跳过目录: ${name}"
            continue
        fi

        local ext="${name##*.}"
        ext=$(echo "${ext}" | tr '[:upper:]' '[:lower:]')
        if [[ "${ext}" != "md" && "${ext}" != "html" ]]; then
            echo "   ⏭️  跳过非目标格式文件: ${name} (仅下载 .md/.html)"
            continue
        fi

        if [[ -z "${download_url}" ]]; then
            echo "   ⚠️  文件 ${name} 没有 downloadUrl，跳过"
            ((fail_count++)) || true
            continue
        fi

        if download_file "${download_url}" "${name}" "${output_dir}"; then
            ((success_count++)) || true
        else
            ((fail_count++)) || true
        fi
    done

    echo ""
    echo "📊 文件下载统计: 成功 ${success_count} 个, 失败 ${fail_count} 个"
}

# 基于外部传入的基础问题补齐 MR 范围约束，生成最终发给服务端的完整 question。
build_request_question() {
    cat <<EOF
${QUESTION}

审查范围约束：
- 审查模式: MR 增量审查
- MR 链接: ${MR_URL}
- 只审查该 MR 对应的增量改动，不要扩展到其他分支历史或仓库无关文件。
EOF
}

stream_request() {
    local final_question
    final_question="$(build_request_question)"

    echo "============================================================"
    echo "🚀 ${PROJECT_NAME} ViewModel 设计审查小助手"
    echo "============================================================"
    echo "🔗 MR 链接: ${MR_URL}"
    echo "📝 最终请求问题内容:"
    echo "${final_question}"
    echo "------------------------------------------------------------"
    echo "🤖 AI 回复:"
    echo ""

    local curl_args=()
    curl_args+=(--data-urlencode "apiKey=${API_KEY}")
    curl_args+=(--data-urlencode "question=${final_question}")

    local tmp_response tmp_curl_err tmp_sse_output
    tmp_response=$(mktemp)
    tmp_curl_err=$(mktemp)
    tmp_sse_output=$(mktemp)
    trap "rm -f '${tmp_response}' '${tmp_curl_err}' '${tmp_sse_output}'" EXIT

    echo "🌐 正在发起请求: ${STREAM_API}"

    local curl_exit_code=0
    local http_code
    http_code=$(curl -sS -N --max-time 1800 \
        "${curl_args[@]}" \
        "${STREAM_API}" \
        -w "%{http_code}" \
        -o "${tmp_sse_output}" \
        2>"${tmp_curl_err}") || curl_exit_code=$?

    if [[ -z "${http_code}" ]]; then
        http_code="000"
    fi

    if [[ ${curl_exit_code} -ne 0 ]]; then
        if [[ ${curl_exit_code} -eq 28 ]] && [[ "${http_code}" =~ ^2[0-9][0-9]$ ]] && [[ -s "${tmp_sse_output}" ]]; then
            echo ""
            echo "⚠️  请求超时，但已收到部分数据 ($(wc -c < "${tmp_sse_output}" | tr -d ' ') bytes)，继续解析..."
        else
            echo ""
            echo "❌ curl 请求失败 (exit code: ${curl_exit_code}, HTTP: ${http_code})"
            if [[ -s "${tmp_curl_err}" ]]; then
                echo "   错误信息: $(cat "${tmp_curl_err}")"
            fi
            rm -f "${tmp_sse_output}"
            return 1
        fi
    fi

    if [[ ! "${http_code}" =~ ^2[0-9][0-9]$ ]]; then
        echo ""
        echo "❌ 请求失败 (HTTP ${http_code})"
        if [[ -s "${tmp_curl_err}" ]]; then
            echo "   错误信息: $(cat "${tmp_curl_err}")"
        fi
        rm -f "${tmp_sse_output}"
        return 1
    fi

    if [[ ! -s "${tmp_sse_output}" ]]; then
        echo ""
        echo "❌ 服务端返回空响应"
        rm -f "${tmp_sse_output}"
        return 1
    fi

    echo "📡 收到响应 ($(wc -c < "${tmp_sse_output}" | tr -d ' ') bytes)，开始解析 SSE 事件..."
    echo ""

    local complete_received=0
    local complete_status=""

    while IFS= read -r line; do
        if [[ ! "${line}" == data:* ]]; then
            continue
        fi

        local json_str="${line#data: }"

        local event_type
        event_type=$(echo "${json_str}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('type',''))" 2>/dev/null || echo "")

        case "${event_type}" in
            "chat:assistant")
                local text
                text=$(echo "${json_str}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('text',''), end='')" 2>/dev/null || echo "")
                if [[ -n "${text}" ]]; then
                    printf "%s" "${text}"
                fi
                ;;

            "chat:generated_files")
                local files
                files=$(echo "${json_str}" | python3 -c "import sys,json; import json as j; print(j.dumps(json.load(sys.stdin).get('files',[])))" 2>/dev/null || echo "[]")
                local count
                count=$(echo "${files}" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
                echo ""
                echo ""
                echo "📦 检测到 ${count} 个生成文件"
                echo "${files}" > "${tmp_response}"
                ;;

            "chat:usage")
                local input_tokens output_tokens
                input_tokens=$(echo "${json_str}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('input_tokens',0))" 2>/dev/null || echo "0")
                output_tokens=$(echo "${json_str}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('output_tokens',0))" 2>/dev/null || echo "0")
                echo ""
                echo ""
                echo "📈 Token 用量: 输入 ${input_tokens}, 输出 ${output_tokens}"
                ;;

            "chat:complete")
                local status
                status=$(echo "${json_str}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('status','unknown'))" 2>/dev/null || echo "unknown")
                complete_received=1
                complete_status="${status}"

                local final_files
                final_files=$(echo "${json_str}" | python3 -c "import sys,json; import json as j; print(j.dumps(json.load(sys.stdin).get('generatedFiles',[])))" 2>/dev/null || echo "[]")
                local final_count
                final_count=$(echo "${final_files}" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")

                if [[ "${final_count}" -gt 0 ]]; then
                    echo "${final_files}" > "${tmp_response}"
                fi

                echo ""
                echo ""
                echo "============================================================"
                if [[ "${status}" == "completed" || "${status}" == "success" ]]; then
                    echo "✅ 任务完成 (状态: ${status})"
                else
                    echo "⚠️  任务结束 (状态: ${status})"
                fi
                ;;
        esac
    done < "${tmp_sse_output}"

    rm -f "${tmp_sse_output}"

    if [[ ${complete_received} -ne 1 ]]; then
        echo ""
        echo "⚠️  未收到任务完成事件（chat:complete），可能因超时截断，尝试处理已收到的文件..."
    elif [[ "${complete_status}" != "completed" && "${complete_status}" != "success" ]]; then
        echo ""
        echo "⚠️  任务未成功完成 (状态: ${complete_status})，尝试处理已收到的文件..."
    fi

    if [[ -f "${tmp_response}" ]] && [[ -s "${tmp_response}" ]]; then
        local saved_files
        saved_files=$(cat "${tmp_response}")
        local saved_count
        saved_count=$(echo "${saved_files}" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")

        if [[ "${saved_count}" -gt 0 ]]; then
            echo ""
            echo "============================================================"
            echo "📁 开始下载文件到: ${OUTPUT_DIR}"
            echo "============================================================"
            process_generated_files "${saved_files}" "${OUTPUT_DIR}"
        else
            echo ""
            echo "ℹ️  本次请求没有生成文件"
        fi
    else
        echo ""
        echo "ℹ️  本次请求没有生成文件"
    fi

    echo ""
    echo "============================================================"
    echo "🎉 完成!"
    echo "============================================================"
}

# ========== 主入口 ==========
main() {
    if ! command -v curl &>/dev/null; then
        echo "❌ 缺少依赖: curl，请先安装"
        exit 1
    fi
    if ! command -v python3 &>/dev/null; then
        echo "❌ 缺少依赖: python3（用于解析 JSON），请先安装"
        exit 1
    fi

    parse_args "$@"
    stream_request
}

main "$@"
