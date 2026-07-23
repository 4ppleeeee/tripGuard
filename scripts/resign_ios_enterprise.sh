#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
VERIFY_SCRIPT="$SCRIPT_DIR/verify_ios_ipa.sh"
DRY_RUN=false
INPUT_IPA=""
OUTPUT_IPA=""
PROFILE_PATH=""
SIGN_IDENTITY=""
EXPECTED_BUNDLE_ID=""
WORK_DIR=""
PROFILE_PLIST_PATH=""
TEAM_ID=""
APP_IDENTIFIER_FROM_PROFILE=""
ENTITLEMENTS_PLIST_PATH=""
APP_PATH=""
APP_NAME=""

usage() {
    cat <<EOF
用法:
  bash scripts/resign_ios_enterprise.sh \
    input-ipa=/path/to/input.ipa \
    output-ipa=/path/to/output.ipa \
    profile=/path/to/InHouse.mobileprovision \
    identity="iPhone Distribution: Tencent Technology (Shenzhen) Co., Ltd" \
    bundleid=com.tencent.microvision.develop

可选参数:
  keep-workdir=true   保留解包目录，便于排查
  QN_BUILD_DRY_RUN=1  仅打印将执行的关键动作
EOF
}

if [[ "${QN_BUILD_DRY_RUN:-0}" == "1" || "${QN_BUILD_DRY_RUN:-false}" == "true" ]]; then
    DRY_RUN=true
fi

KEEP_WORKDIR=false

fail() {
    echo "❌ $1"
    exit 1
}

print_step() {
    echo "$1"
}

run_command() {
    printf '  '
    printf '%q ' "$@"
    printf '\n'
    if [ "$DRY_RUN" = true ]; then
        return 0
    fi
    "$@"
}

cleanup() {
    if [ "$KEEP_WORKDIR" != true ] && [ -n "$WORK_DIR" ] && [ -d "$WORK_DIR" ]; then
        rm -rf "$WORK_DIR"
    fi
}

trap cleanup EXIT

parse_args() {
    for arg in "$@"; do
        case "$arg" in
            input-ipa=*) INPUT_IPA="${arg#input-ipa=}" ;;
            output-ipa=*) OUTPUT_IPA="${arg#output-ipa=}" ;;
            profile=*) PROFILE_PATH="${arg#profile=}" ;;
            identity=*) SIGN_IDENTITY="${arg#identity=}" ;;
            bundleid=*) EXPECTED_BUNDLE_ID="${arg#bundleid=}" ;;
            keep-workdir=true) KEEP_WORKDIR=true ;;
            keep-workdir=false) KEEP_WORKDIR=false ;;
            -h|--help) usage; exit 0 ;;
            *) fail "不支持的参数: $arg" ;;
        esac
    done

    [ -n "$INPUT_IPA" ] || fail "缺少 input-ipa=..."
    [ -f "$INPUT_IPA" ] || fail "输入 IPA 不存在: $INPUT_IPA"
    [ -n "$OUTPUT_IPA" ] || fail "缺少 output-ipa=..."
    [ -n "$PROFILE_PATH" ] || fail "缺少 profile=..."
    [ -f "$PROFILE_PATH" ] || fail "Provisioning profile 不存在: $PROFILE_PATH"
    [ -n "$SIGN_IDENTITY" ] || fail "缺少 identity=..."
    [ -n "$EXPECTED_BUNDLE_ID" ] || fail "缺少 bundleid=..."
}

read_plist_value() {
    local plist_path="$1"
    local key_path="$2"

    /usr/libexec/PlistBuddy -c "Print ${key_path}" "$plist_path" 2>/dev/null || true
}

prepare_profile_metadata() {
    PROFILE_PLIST_PATH="$WORK_DIR/profile.plist"
    if [ "$DRY_RUN" = true ]; then
        print_step "📄 Dry-run: 跳过 profile 解码，按传入 bundleid 规划重签动作"
        TEAM_ID="UNKNOWN_TEAM"
        APP_IDENTIFIER_FROM_PROFILE="*.UNKNOWN"
        return
    fi

    security cms -D -i "$PROFILE_PATH" > "$PROFILE_PLIST_PATH"
    TEAM_ID="$(read_plist_value "$PROFILE_PLIST_PATH" ':TeamIdentifier:0')"
    APP_IDENTIFIER_FROM_PROFILE="$(read_plist_value "$PROFILE_PLIST_PATH" ':Entitlements:application-identifier')"

    [ -n "$TEAM_ID" ] || fail "无法从 profile 解析 TeamIdentifier: $PROFILE_PATH"
    [ -n "$APP_IDENTIFIER_FROM_PROFILE" ] || fail "无法从 profile 解析 application-identifier: $PROFILE_PATH"
}

ensure_profile_matches_bundle_id() {
    if [ "$DRY_RUN" = true ]; then
        return
    fi

    local suffix="${APP_IDENTIFIER_FROM_PROFILE#*.}"
    if [ "$suffix" = "*" ]; then
        return
    fi

    if [ "$suffix" != "$EXPECTED_BUNDLE_ID" ]; then
        fail "profile 与目标 Bundle ID 不匹配。profile: ${APP_IDENTIFIER_FROM_PROFILE}，bundleid: ${EXPECTED_BUNDLE_ID}"
    fi
}

unpack_ipa() {
    WORK_DIR="$(mktemp -d "$ROOT_DIR/archives/ios/resign-ipa.XXXXXX")"
    printf '  '
    printf '%q ' unzip -q "$INPUT_IPA" -d "$WORK_DIR"
    printf '\n'
    unzip -q "$INPUT_IPA" -d "$WORK_DIR"

    APP_PATH="$(find "$WORK_DIR/Payload" -maxdepth 1 -type d -name '*.app' | head -n 1)"
    [ -n "$APP_PATH" ] || fail "解包后未找到主 app"
    APP_NAME="$(basename "$APP_PATH")"
}

remove_invalid_framework_profiles() {
    local framework_profile_output=""
    framework_profile_output="$(find "$APP_PATH/Frameworks" -type f -name 'embedded.mobileprovision' 2>/dev/null | sort || true)"

    if [ -z "$framework_profile_output" ]; then
        return
    fi

    print_step "🧹 删除 framework 中误注入的 embedded.mobileprovision"
    while IFS= read -r profile_path; do
        [ -n "$profile_path" ] || continue
        run_command rm -f "$profile_path"
    done <<EOF
$framework_profile_output
EOF
}

update_main_app_bundle_identifier() {
    local current_bundle_id
    current_bundle_id="$(read_plist_value "$APP_PATH/Info.plist" ':CFBundleIdentifier')"

    if [ "$current_bundle_id" = "$EXPECTED_BUNDLE_ID" ]; then
        return
    fi

    print_step "🪪 修正主 app Bundle ID: $EXPECTED_BUNDLE_ID"

    if [ "$DRY_RUN" = true ]; then
        return
    fi

    /usr/libexec/PlistBuddy -c "Set :CFBundleIdentifier $EXPECTED_BUNDLE_ID" "$APP_PATH/Info.plist" 2>/dev/null \
        || /usr/libexec/PlistBuddy -c "Add :CFBundleIdentifier string $EXPECTED_BUNDLE_ID" "$APP_PATH/Info.plist"
}

replace_main_profile() {
    print_step "📲 替换主 app embedded.mobileprovision"
    run_command cp "$PROFILE_PATH" "$APP_PATH/embedded.mobileprovision"
}

build_entitlements_plist() {
    ENTITLEMENTS_PLIST_PATH="$WORK_DIR/entitlements.plist"

    if [ "$DRY_RUN" = true ]; then
        return
    fi

    python3 - "$PROFILE_PLIST_PATH" "$ENTITLEMENTS_PLIST_PATH" "$EXPECTED_BUNDLE_ID" "$TEAM_ID" <<'PY'
from pathlib import Path
import plistlib
import sys

profile_path = Path(sys.argv[1])
out_path = Path(sys.argv[2])
bundle_id = sys.argv[3]
team_id = sys.argv[4]

profile = plistlib.load(profile_path.open('rb'))
entitlements = dict(profile.get('Entitlements', {}))

app_identifier = entitlements.get('application-identifier', '')
if app_identifier.endswith('.*'):
    entitlements['application-identifier'] = f'{team_id}.{bundle_id}'
else:
    entitlements['application-identifier'] = app_identifier or f'{team_id}.{bundle_id}'

keychain_groups = entitlements.get('keychain-access-groups')
if isinstance(keychain_groups, list):
    normalized_groups = []
    for group in keychain_groups:
        if group == f'{team_id}.*':
            normalized_groups.append(f'{team_id}.{bundle_id}')
        else:
            normalized_groups.append(group)
    entitlements['keychain-access-groups'] = normalized_groups

out_path.parent.mkdir(parents=True, exist_ok=True)
with out_path.open('wb') as fp:
    plistlib.dump(entitlements, fp)
PY
}

sign_frameworks() {
    if [ ! -d "$APP_PATH/Frameworks" ]; then
        return
    fi

    print_step "🔏 重签内嵌 frameworks"
    local framework_path=""
    while IFS= read -r framework_path; do
        run_command codesign --force --sign "$SIGN_IDENTITY" --timestamp=none "$framework_path"
    done < <(find "$APP_PATH/Frameworks" -maxdepth 1 -type d -name '*.framework' | sort)
}

sign_bundles_if_needed() {
    print_step "🔏 重签主 app 内的资源 bundle（若含代码签名）"
    local bundle_path=""
    while IFS= read -r bundle_path; do
        if [ -d "$bundle_path/_CodeSignature" ]; then
            run_command codesign --force --sign "$SIGN_IDENTITY" --timestamp=none "$bundle_path"
        fi
    done < <(find "$APP_PATH" -maxdepth 2 -type d -name '*.bundle' | sort)
}

sign_plugins_if_needed() {
    if [ ! -d "$APP_PATH/PlugIns" ]; then
        return
    fi

    print_step "🔏 重签 appex"
    local appex_path=""
    while IFS= read -r appex_path; do
        run_command cp "$PROFILE_PATH" "$appex_path/embedded.mobileprovision"
        run_command codesign --force --sign "$SIGN_IDENTITY" --timestamp=none "$appex_path"
    done < <(find "$APP_PATH/PlugIns" -maxdepth 1 -type d -name '*.appex' | sort)
}

sign_main_app() {
    print_step "🔏 重签主 app"
    if [ "$DRY_RUN" = true ]; then
        run_command codesign --force --sign "$SIGN_IDENTITY" --entitlements "$WORK_DIR/entitlements.plist" --timestamp=none "$APP_PATH"
        return
    fi

    run_command codesign --force --sign "$SIGN_IDENTITY" --entitlements "$ENTITLEMENTS_PLIST_PATH" --timestamp=none "$APP_PATH"
}

pack_output_ipa() {
    local output_dir
    output_dir="$(dirname "$OUTPUT_IPA")"
    run_command mkdir -p "$output_dir"

    if [ "$DRY_RUN" != true ]; then
        rm -f "$OUTPUT_IPA"
    fi

    print_step "📦 重新打包 IPA"
    (
        cd "$WORK_DIR"
        run_command zip -qry "$OUTPUT_IPA" Payload
    )
}

run_post_verify() {
    print_step "📦 重签完成后将执行 IPA 自检"
    run_command bash "$VERIFY_SCRIPT" input-ipa="$OUTPUT_IPA" mode=structure expected-bundleid="$EXPECTED_BUNDLE_ID"
}

main() {
    parse_args "$@"
    unpack_ipa
    prepare_profile_metadata
    ensure_profile_matches_bundle_id
    remove_invalid_framework_profiles
    update_main_app_bundle_identifier
    replace_main_profile
    build_entitlements_plist
    sign_frameworks
    sign_bundles_if_needed
    sign_plugins_if_needed
    sign_main_app
    pack_output_ipa
    run_post_verify
    echo "✅ 企业重签处理完成: $OUTPUT_IPA"
}

main "$@"
