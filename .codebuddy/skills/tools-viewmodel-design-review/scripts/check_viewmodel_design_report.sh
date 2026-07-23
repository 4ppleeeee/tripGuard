#!/bin/bash
set -euo pipefail

# ViewModel 设计审查报告检查脚本
#
# 功能：读取 ViewModel 审查 Markdown 报告，判断审查结果是否通过。
#
# 配置：报告文件名从同级 config.json 读取，迁移到新工程时无需修改本脚本。
#
# 用法：
#   bash check_viewmodel_design_report.sh [--result-only] [报告路径]
#
# 默认模式退出码：
#   0  审查通过 或 未命中审查范围
#   1  审查不通过
#   2  有条件通过
#   3  无法判定
#   4  报告文件不存在
#   5  参数错误

# ========== 路径与配置 ==========

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SKILL_ROOT="$(dirname "${SCRIPT_DIR}")"
# 项目根目录：优先用 git，fallback 用相对路径（skill 在 .codebuddy/skills/<name>/scripts/）
if PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)"; then
    : # 成功获取
else
    PROJECT_ROOT="$(cd "${SKILL_ROOT}/../../../.." && pwd)"
fi
CONFIG_FILE="${SKILL_ROOT}/config.json"

# 从 config.json 读取配置
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

# 从 config.json 读取报告路径
REPORT_OUTPUT_DIR="$(read_config "report.output_dir" "./report")"
REPORT_MD_FILENAME="$(read_config "report.md_filename" "ViewModel-design-report.md")"

# 拼接默认报告完整路径
if [[ "${REPORT_OUTPUT_DIR}" != /* ]]; then
    DEFAULT_REPORT_PATH="${PROJECT_ROOT}/${REPORT_OUTPUT_DIR}/${REPORT_MD_FILENAME}"
else
    DEFAULT_REPORT_PATH="${REPORT_OUTPUT_DIR}/${REPORT_MD_FILENAME}"
fi

# ========== 函数 ==========

usage() {
  cat <<EOF
用法：$(basename "$0") [--result-only] [报告路径]

功能：读取 ViewModel 审查 Markdown 报告，期望审查结果为"通过"。
      默认模式下，如果不是"通过"，脚本将以失败状态退出。
      使用 --result-only 时，只输出机器可读结果并始终返回 0，适用于 CI 流水线。

参数：
  --result-only  只输出 PASS / FAIL / CONDITIONAL / SKIPPED / UNKNOWN，并始终返回 0
  报告路径      可选，默认读取：${DEFAULT_REPORT_PATH}

配置文件：
  ${CONFIG_FILE}
  报告文件名从此配置读取，迁移时无需修改本脚本。

默认模式退出码：
  0  审查通过 或 未命中审查范围（期望状态）
  1  审查不通过（失败）
  2  有条件通过（失败）
  3  无法判定（失败）
  4  报告文件不存在
  5  参数错误

判定优先级：
  1. 优先读取 "是否可合入" 字段
  2. 其次读取 "判定" 字段
EOF
}

# 清洗字段值中的 Markdown 标记和多余空白，避免不同平台 sed 对 UTF-8 字符处理不一致。
trim_value() {
  local raw_value="$1"
  if command -v python3 &>/dev/null; then
    python3 - "$raw_value" <<'PY'
import re
import sys

value = sys.argv[1]
for marker in ("`", "**", "❌", "✅", "⚠️", "❓"):
    value = value.replace(marker, "")
value = re.sub(r"\s+", " ", value).strip()
print(value, end="")
PY
  else
    printf '%s' "$raw_value" | tr -d '`' | sed 's/\*\*//g' | awk '{$1=$1; print}'
  fi
}

# 提取报告中指定字段的值，使用 Python 处理全角冒号与加粗标记，兼容 macOS / Linux。
extract_field_value() {
  local field_name="$1"
  local report_path="$2"

  if command -v python3 &>/dev/null; then
    python3 - "$field_name" "$report_path" <<'PY'
import pathlib
import re
import sys

field_name = sys.argv[1]
report_path = pathlib.Path(sys.argv[2])
pattern = re.compile(
    rf'^\s*(?:[-*]|\d+[.)])?\s*(?:\*\*)?{re.escape(field_name)}(?:\*\*)?\s*[:：]\s*(.*)$'
)

for line in report_path.read_text(encoding="utf-8").splitlines():
    match = pattern.match(line)
    if match:
        print(match.group(1), end="")
        sys.exit(0)

sys.exit(1)
PY
    return $?
  fi

  local matched_line=""
  matched_line="$(grep -m1 -E "^[[:space:]]*([-*]|[0-9]+[.)])?[[:space:]]*(\\*\\*)?${field_name}(\\*\\*)?[[:space:]]*:" "$report_path" || true)"

  if [[ -z "$matched_line" ]]; then
    return 1
  fi

  printf '%s' "$matched_line" | sed -E "s/^[[:space:]]*([-*]|[0-9]+[.)])?[[:space:]]*(\\*\\*)?${field_name}(\\*\\*)?[[:space:]]*:[[:space:]]*//"
}

# 归一化判定值，去掉括号说明和空白，确保中英文结果都能稳定映射。
normalize_result_value() {
  local raw_value="$1"
  if command -v python3 &>/dev/null; then
    python3 - "$raw_value" <<'PY'
import re
import sys

value = sys.argv[1]
value = re.sub(r'[（(][^）)]*[）)]', '', value)
value = re.sub(r'\s+', '', value)
print(value, end="")
PY
  else
    printf '%s' "$raw_value" | sed -E 's/[[:space:]]+//g'
  fi
}

resolve_result() {
  local field_value="$1"
  local normalized_value=""
  local normalized_lc=""

  normalized_value="$(normalize_result_value "$field_value")"
  normalized_lc="$(printf '%s' "$normalized_value" | tr '[:upper:]' '[:lower:]')"

  case "$normalized_value" in
    是*|通过*|建议通过*|可合入*|可合并*)       echo "PASS"; return 0 ;;
    否*|不通过*|不建议通过*|不可合入*|不可合并*) echo "FAIL"; return 0 ;;
    有条件*|条件通过*|部分通过*|需改进*|待改进*) echo "CONDITIONAL"; return 0 ;;
    未命中*|不涉及*|无需审查*|跳过*)           echo "SKIPPED"; return 0 ;;
    待评估*|待确认*|未知*|未填写*|"" )         echo "UNKNOWN"; return 0 ;;
  esac

  case "$normalized_lc" in
    pass*|approved*)                              echo "PASS"; return 0 ;;
    fail*|rejected*)                              echo "FAIL"; return 0 ;;
    conditional*)                                 echo "CONDITIONAL"; return 0 ;;
    skip*|notapplicable*|not_applicable*)          echo "SKIPPED"; return 0 ;;
    n/a|na|unknown*|pending*|"")                  echo "UNKNOWN"; return 0 ;;
  esac

  echo "UNKNOWN"
  return 0
}

result_exit_code() {
  local result="$1"
  case "$result" in
    PASS)        echo 0 ;;
    SKIPPED)     echo 0 ;;
    FAIL)        echo 1 ;;
    CONDITIONAL) echo 2 ;;
    *)           echo 3 ;;
  esac
}

print_human_readable_result() {
  local result="$1"
  local field_name="$2"
  local field_value="$3"
  local report_path="$4"

  case "$result" in
    PASS)
      echo "✅ ViewModel 审查通过"
      echo "- 报告文件：${report_path}"
      echo "- 判定字段：${field_name}"
      echo "- 判定值：${field_value}"
      echo "- 期望值：通过"
      echo "- 结果：符合期望"
      ;;
    SKIPPED)
      echo "⚪ 未命中审查范围"
      echo "- 报告文件：${report_path}"
      echo "- 判定字段：${field_name}"
      echo "- 判定值：${field_value}"
      echo "- 结果：本次 MR 修改不涉及 ViewModel 相关文件，无需审查，放行"
      ;;
    FAIL)
      echo "❌ ViewModel 审查不通过"
      echo "- 报告文件：${report_path}"
      echo "- 判定字段：${field_name}"
      echo "- 判定值：${field_value}"
      echo "- 期望值：通过"
      echo "- 结果：不符合期望（存在硬性违规）"
      echo ""
      echo "请修复报告中标注的硬性问题后重新提交审查。"
      ;;
    CONDITIONAL)
      echo "⚠️ ViewModel 审查为有条件通过"
      echo "- 报告文件：${report_path}"
      echo "- 判定字段：${field_name}"
      echo "- 判定值：${field_value}"
      echo "- 期望值：通过"
      echo "- 结果：不符合期望（存在需改进项）"
      echo ""
      echo "请修复报告中标注的改进建议后重新提交审查。"
      ;;
    *)
      echo "❓ 无法判定 ViewModel 审查结果"
      echo "- 报告文件：${report_path}"
      echo "- 最近命中的字段：${field_name:-无}"
      echo "- 字段值：${field_value:-无}"
      echo "- 期望值：通过"
      echo "- 结果：无法判定（报告格式可能异常）"
      echo ""
      echo "请检查报告格式是否正确，确保包含 '判定' 或 '是否可合入' 字段。"
      ;;
  esac
}

finalize_result() {
  local result="$1"
  local field_name="$2"
  local field_value="$3"
  local report_path="$4"
  local mode="$5"

  if [[ "$mode" == "result_only" ]]; then
    echo "$result"
    exit 0
  fi

  print_human_readable_result "$result" "$field_name" "$field_value" "$report_path"
  exit "$(result_exit_code "$result")"
}

# ========== 主逻辑 ==========

MODE="default"
POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)     usage; exit 0 ;;
    --result-only) MODE="result_only"; shift ;;
    --)
      shift
      while [[ $# -gt 0 ]]; do POSITIONAL_ARGS+=("$1"); shift; done
      ;;
    -*)
      echo "参数错误：不支持的选项 $1"
      usage
      exit 5
      ;;
    *)
      POSITIONAL_ARGS+=("$1")
      shift
      ;;
  esac
done

if [[ ${#POSITIONAL_ARGS[@]} -gt 1 ]]; then
  echo "参数错误：最多只允许传入一个报告路径。"
  usage
  exit 5
fi

REPORT_PATH="${POSITIONAL_ARGS[0]:-$DEFAULT_REPORT_PATH}"

if [[ ! -f "$REPORT_PATH" ]]; then
  if [[ "$MODE" == "result_only" ]]; then
    echo "UNKNOWN"
    exit 0
  fi
  echo "报告文件不存在：${REPORT_PATH}"
  exit 4
fi

FIELD_NAME=""
FIELD_VALUE=""
RESULT="UNKNOWN"

if raw_merge_value="$(extract_field_value "是否可合入" "$REPORT_PATH" 2>/dev/null)"; then
  FIELD_NAME="是否可合入"
  FIELD_VALUE="$(trim_value "$raw_merge_value")"
  RESULT="$(resolve_result "$FIELD_VALUE")"
fi

if [[ "$RESULT" == "UNKNOWN" ]]; then
  if raw_judgement_value="$(extract_field_value "判定" "$REPORT_PATH" 2>/dev/null)"; then
    FIELD_NAME="判定"
    FIELD_VALUE="$(trim_value "$raw_judgement_value")"
    RESULT="$(resolve_result "$FIELD_VALUE")"
  fi
fi

finalize_result "$RESULT" "$FIELD_NAME" "$FIELD_VALUE" "$REPORT_PATH" "$MODE"
