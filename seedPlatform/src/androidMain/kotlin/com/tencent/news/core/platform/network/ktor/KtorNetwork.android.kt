package com.tencent.news.core.platform.network.ktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttpConfig
import io.ktor.client.engine.okhttp.OkHttpEngine

actual fun createEngine(): HttpClientEngine {
    return OkHttpEngine(OkHttpConfig())
}
