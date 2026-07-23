package com.tencent.news.core.platform.network.ktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ohos.OhosHttpConfig
import io.ktor.client.engine.ohos.OhosHttpEngine

actual fun createEngine(): HttpClientEngine {
    return OhosHttpEngine(OhosHttpConfig().apply {
        verbose = true
    })
}
