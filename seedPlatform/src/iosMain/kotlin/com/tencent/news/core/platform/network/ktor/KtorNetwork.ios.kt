package com.tencent.news.core.platform.network.ktor

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun createEngine(): HttpClientEngine {
    return Darwin.create {}
}
