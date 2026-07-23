package com.tencent.news.core.platform

object AndroidRuntimeProvider {
    var shiplyStringProvider: (key: String, defaultValue: String) -> String = { _, defaultValue ->
        defaultValue
    }

    var shiplySwitchProvider: (key: String, defaultValue: Boolean) -> Boolean = { _, defaultValue ->
        defaultValue
    }

    var tabExpIntProvider: (key: String, defaultValue: Int) -> Int = { _, defaultValue ->
        defaultValue
    }
}
