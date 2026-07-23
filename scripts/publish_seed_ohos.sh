#!/bin/bash
set -euo pipefail

export QN_COMPAT_BUILD_TYPE=ohos

./gradlew :umbrella:publishSeedOhosArtifact \
  -Pkmm.seed.fat=true \
  -x :umbrella:knoiBinariesPublishForbiddenTask \
  --no-daemon --console=plain
