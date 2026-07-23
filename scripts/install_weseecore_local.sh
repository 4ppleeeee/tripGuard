#!/bin/bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH
./gradlew :startup:checkSchemeDoc :androidApp:assembleDebug --stacktrace
adb push ./androidApp/build/outputs/apk/debug/androidApp-debug.apk /sdcard/Android/data/com.tencent.weishi/files/weseecore.apk
adb shell am force-stop com.tencent.weishi
adb shell am start -n "com.tencent.weishi/com.tencent.weishi.ui.debug.WeSeeCorePageActivity"
