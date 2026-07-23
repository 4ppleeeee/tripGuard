package com.tencent.kmm.demo.setup

import android.content.res.Configuration

internal fun notifySystemConfigurationChanged(configuration: Configuration) {
    // Kept as a host hook. Real apps can sync host user configuration here.
}

internal fun syncSystemConfigurationOnForeground() {
    // Kept as a host hook. Real apps can sync host user configuration here.
}

internal fun syncDarkModeOnStartup() {
    // Kept as a host hook. Real apps can sync host user configuration here.
}

internal fun syncBigFontModeOnStartup() {
    // Kept as a host hook. Real apps can sync host user configuration here.
}

internal fun syncAutoPlayNextOnStartup() {
    // Kept as a host hook. Real apps can sync host user configuration here.
}
