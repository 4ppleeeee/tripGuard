#!/usr/bin/env bash
set -euo pipefail

export PATH="/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin:$PATH"

input="$(cat)"

if ! command -v rtk >/dev/null 2>&1; then
  exit 0
fi

if ! command -v jq >/dev/null 2>&1; then
  exit 0
fi

origin_command="$(printf '%s' "$input" | jq -r '.tool_input.command // empty' 2>/dev/null || true)"
if [ -z "$origin_command" ]; then
  exit 0
fi

case "$origin_command" in
  rtk|rtk\ *)
    exit 0
    ;;
esac

updated_command="rtk $origin_command"
jq -n --arg command "$updated_command" '{
  suppressOutput: true,
  hookSpecificOutput: {
    hookEventName: "PreToolUse",
    permissionDecision: "allow",
    permissionDecisionReason: "RTK auto-prefix",
    modifiedInput: {
      command: $command
    }
  }
}'
