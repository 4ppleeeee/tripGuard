#!/bin/bash
set -Eeuo pipefail

export QN_COMPAT_BUILD_TYPE=android
export QN_PUBLISH_RELEASE_ONLY=true

if [[ "$(uname -s)" == "Darwin" ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "=== Seed Android publish: Kotlin Gradle Plugin resolution ==="
./gradlew :build-logic:convention:dependencyInsight \
  --dependency org.jetbrains.kotlin:kotlin-gradle-plugin \
  --configuration runtimeClasspath \
  --no-daemon --console=plain

echo "=== Seed Android publish: KSP resolution ==="
./gradlew :build-logic:convention:dependencyInsight \
  --dependency com.google.devtools.ksp \
  --configuration runtimeClasspath \
  --no-daemon --console=plain

./gradlew :umbrella:publishSeedAndroidArtifact --no-daemon --console=plain
