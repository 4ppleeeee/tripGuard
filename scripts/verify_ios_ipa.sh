#!/bin/bash
set -euo pipefail

INPUT_IPA=""
MODE="full"
EXPECTED_BUNDLE_ID=""
EXPECTED_TEAM_ID=""
KEEP_UNPACKED=false
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
WORK_DIR=""
APP_PATH=""
APP_BUNDLE_ID=""
PROFILE_PLIST_PATH=""
PROFILE_TEAM_ID=""
PROFILE_APPLICATION_IDENTIFIER=""

usage() {
    cat <<EOF
用法:
  bash scripts/verify_ios_ipa.sh input-ipa=/path/to/app.ipa [mode=full|structure] [expected-bundleid=com.example.app] [expected-team=TEAMID] [keep-unpacked=true]

说明:
  - structure: 仅检查 IPA 结构、主 app Bundle ID、framework 中是否误注入 embedded.mobileprovision
  - full: 在 structure 基础上继续检查 provisioning profile 与 codesign 一致性
EOF
}

fail() {
    echo "❌ $1"
    exit 1
}

print_info() {
    echo "ℹ️  $1"
}

cleanup() {
    if [ "$KEEP_UNPACKED" != true ] && [ -n "$WORK_DIR" ] && [ -d "$WORK_DIR" ]; then
        rm -rf "$WORK_DIR"
    fi
}

trap cleanup EXIT

parse_args() {
    for arg in "$@"; do
        case "$arg" in
            input-ipa=*) INPUT_IPA="${arg#input-ipa=}" ;;
            mode=*) MODE="${arg#mode=}" ;;
            expected-bundleid=*) EXPECTED_BUNDLE_ID="${arg#expected-bundleid=}" ;;
            expected-team=*) EXPECTED_TEAM_ID="${arg#expected-team=}" ;;
            keep-unpacked=true) KEEP_UNPACKED=true ;;
            keep-unpacked=false) KEEP_UNPACKED=false ;;
            -h|--help) usage; exit 0 ;;
            *) fail "不支持的参数: $arg" ;;
        esac
    done

    [ -n "$INPUT_IPA" ] || fail "缺少 input-ipa=..."
    [ -f "$INPUT_IPA" ] || fail "IPA 文件不存在: $INPUT_IPA"

    case "$MODE" in
        full|structure) ;;
        *) fail "mode 仅支持 full 或 structure，当前为: $MODE" ;;
    esac
}

read_plist_value() {
    local plist_path="$1"
    local key_path="$2"

    /usr/libexec/PlistBuddy -c "Print ${key_path}" "$plist_path" 2>/dev/null || true
}

find_main_app() {
    local payload_dir="$WORK_DIR/Payload"
    [ -d "$payload_dir" ] || fail "IPA 中未找到 Payload 目录"

    local app_count=0
    local app_path=""
    local first_app_path=""
    while IFS= read -r app_path; do
        if [ -z "$first_app_path" ]; then
            first_app_path="$app_path"
        fi
        app_count=$((app_count + 1))
    done < <(find "$payload_dir" -maxdepth 1 -type d -name '*.app' | sort)

    if [ "$app_count" -ne 1 ]; then
        fail "Payload 下应且只能存在一个主 app，实际找到 ${app_count} 个"
    fi

    APP_PATH="$first_app_path"
    [ -f "$APP_PATH/Info.plist" ] || fail "主 app 缺少 Info.plist: $APP_PATH"
}

validate_embedded_bundle_identifiers_unique() {
    local duplicate_output=""
    duplicate_output="$(python3 - "$APP_PATH" <<'PY'
from pathlib import Path
import plistlib
import sys

app_path = Path(sys.argv[1])
plist_paths = [app_path / 'Info.plist']
frameworks_dir = app_path / 'Frameworks'
plugins_dir = app_path / 'PlugIns'

if frameworks_dir.is_dir():
    plist_paths.extend(sorted(frameworks_dir.glob('*.framework/Info.plist')))

if plugins_dir.is_dir():
    plist_paths.extend(sorted(plugins_dir.glob('*.appex/Info.plist')))

seen = {}
duplicates = []
for plist_path in plist_paths:
    if not plist_path.is_file():
        continue
    with plist_path.open('rb') as fp:
        info = plistlib.load(fp)
    bundle_id = info.get('CFBundleIdentifier', '')
    if not bundle_id:
        continue
    bundle_path = str(plist_path.parent)
    if bundle_id in seen:
        duplicates.append((bundle_id, seen[bundle_id], bundle_path))
        continue
    seen[bundle_id] = bundle_path

for bundle_id, first_path, second_path in duplicates:
    print(f'{bundle_id}\t{first_path}\t{second_path}')
PY
)"

    if [ -z "$duplicate_output" ]; then
        return
    fi

    echo "发现重复的 CFBundleIdentifier，检测到以下冲突:"
    while IFS=$'\t' read -r bundle_id first_path second_path; do
        [ -n "$bundle_id" ] || continue
        echo "  - ${bundle_id}"
        echo "    * ${first_path}"
        echo "    * ${second_path}"
    done <<EOF
$duplicate_output
EOF
    exit 1
}

collect_structure_diagnostics() {
    APP_BUNDLE_ID="$(read_plist_value "$APP_PATH/Info.plist" ':CFBundleIdentifier')"
    [ -n "$APP_BUNDLE_ID" ] || fail "无法读取主 app 的 CFBundleIdentifier: $APP_PATH/Info.plist"

    print_info "主 app: $APP_PATH"
    print_info "CFBundleIdentifier: $APP_BUNDLE_ID"

    if [ -n "$EXPECTED_BUNDLE_ID" ] && [ "$APP_BUNDLE_ID" != "$EXPECTED_BUNDLE_ID" ]; then
        fail "CFBundleIdentifier 与预期不一致。当前: ${APP_BUNDLE_ID}，预期: ${EXPECTED_BUNDLE_ID}"
    fi

    [ -f "$APP_PATH/embedded.mobileprovision" ] || fail "主 app 缺少 embedded.mobileprovision"

    validate_embedded_bundle_identifiers_unique

    local framework_profile_output=""
    framework_profile_output="$(find "$APP_PATH/Frameworks" -type f -name 'embedded.mobileprovision' 2>/dev/null | sort || true)"
    if [ -n "$framework_profile_output" ]; then
        echo "framework 内不应包含 embedded.mobileprovision，检测到以下异常文件:"
        while IFS= read -r framework_profile_path; do
            [ -n "$framework_profile_path" ] || continue
            echo "  - $framework_profile_path"
        done <<EOF
$framework_profile_output
EOF
        exit 1
    fi

    local appex_path=""
    while IFS= read -r appex_path; do
        [ -n "$appex_path" ] || continue
        if [ ! -f "$appex_path/embedded.mobileprovision" ]; then
            fail "发现 appex 但缺少 embedded.mobileprovision: $appex_path"
        fi
    done < <(find "$APP_PATH/PlugIns" -type d -name '*.appex' 2>/dev/null | sort)
}

parse_profile_metadata() {
    PROFILE_PLIST_PATH="$WORK_DIR/profile.plist"
    if ! security cms -D -i "$APP_PATH/embedded.mobileprovision" > "$PROFILE_PLIST_PATH" 2>/dev/null; then
        fail "无法解析主 app 的 embedded.mobileprovision，请确认它是合法的 provisioning profile"
    fi

    PROFILE_TEAM_ID="$(read_plist_value "$PROFILE_PLIST_PATH" ':TeamIdentifier:0')"
    PROFILE_APPLICATION_IDENTIFIER="$(read_plist_value "$PROFILE_PLIST_PATH" ':Entitlements:application-identifier')"

    [ -n "$PROFILE_TEAM_ID" ] || fail "无法从 provisioning profile 读取 TeamIdentifier"
    [ -n "$PROFILE_APPLICATION_IDENTIFIER" ] || fail "无法从 provisioning profile 读取 application-identifier"

    print_info "Profile TeamIdentifier: $PROFILE_TEAM_ID"
    print_info "Profile application-identifier: $PROFILE_APPLICATION_IDENTIFIER"

    if [ -n "$EXPECTED_TEAM_ID" ] && [ "$PROFILE_TEAM_ID" != "$EXPECTED_TEAM_ID" ]; then
        fail "Provisioning profile TeamIdentifier 与预期不一致。当前: ${PROFILE_TEAM_ID}，预期: ${EXPECTED_TEAM_ID}"
    fi
}

profile_matches_bundle_id() {
    local profile_app_id="$1"
    local bundle_id="$2"

    local suffix="${profile_app_id#*.}"
    if [ "$suffix" = "*" ]; then
        return 0
    fi

    [ "$suffix" = "$bundle_id" ]
}

validate_profile_binding() {
    if ! profile_matches_bundle_id "$PROFILE_APPLICATION_IDENTIFIER" "$APP_BUNDLE_ID"; then
        fail "Provisioning profile 与主 app Bundle ID 不匹配。Profile: ${PROFILE_APPLICATION_IDENTIFIER}，Bundle ID: ${APP_BUNDLE_ID}"
    fi
}

extract_codesign_field() {
    local codesign_output="$1"
    local field_name="$2"

    echo "$codesign_output" | sed -n "s/^${field_name}=//p" | head -n 1
}

validate_codesign() {
    local codesign_output
    codesign_output="$(codesign -dvvv "$APP_PATH" 2>&1 | cat)"

    local codesign_identifier
    codesign_identifier="$(extract_codesign_field "$codesign_output" 'Identifier')"
    local codesign_team
    codesign_team="$(extract_codesign_field "$codesign_output" 'TeamIdentifier')"

    [ -n "$codesign_identifier" ] || fail "无法从 codesign 输出读取主 app Identifier"
    [ -n "$codesign_team" ] || fail "无法从 codesign 输出读取主 app TeamIdentifier"

    print_info "CodeSign Identifier: $codesign_identifier"
    print_info "CodeSign TeamIdentifier: $codesign_team"

    if [ "$codesign_identifier" != "$APP_BUNDLE_ID" ]; then
        fail "codesign Identifier 与 Info.plist 中的 CFBundleIdentifier 不一致。codesign: ${codesign_identifier}，Info.plist: ${APP_BUNDLE_ID}"
    fi

    if [ "$codesign_team" != "$PROFILE_TEAM_ID" ]; then
        fail "codesign TeamIdentifier 与 provisioning profile TeamIdentifier 不一致。codesign: ${codesign_team}，profile: ${PROFILE_TEAM_ID}"
    fi

    if ! codesign -vvv --verify --deep --strict "$APP_PATH" >/dev/null 2>&1; then
        fail "codesign --verify --deep --strict 校验失败: $APP_PATH"
    fi
}

main() {
    parse_args "$@"

    WORK_DIR="$(mktemp -d "$ROOT_DIR/archives/ios/verify-ipa.XXXXXX")"
    unzip -q "$INPUT_IPA" -d "$WORK_DIR"

    find_main_app
    collect_structure_diagnostics

    if [ "$MODE" = "structure" ]; then
        echo "✅ IPA 结构检查通过"
        return 0
    fi

    parse_profile_metadata
    validate_profile_binding
    validate_codesign

    echo "✅ IPA 完整检查通过"
}

main "$@"
