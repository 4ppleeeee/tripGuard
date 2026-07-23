#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

# Keep the Seed Android artifact equivalent to qnCommon's fat-AAR release path.
export QN_COMPAT_BUILD_TYPE=android

./gradlew :umbrella:publishQnkmmAndroidReleasePublicationToMavenLocal --no-daemon --console=plain
