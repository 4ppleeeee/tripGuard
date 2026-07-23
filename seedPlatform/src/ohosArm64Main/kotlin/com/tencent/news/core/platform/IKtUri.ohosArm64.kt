package com.tencent.news.core.platform

import com.tencent.news.core.extension.concatPrefix
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IAppUri
import com.tencent.news.core.platform.api.IKtMutableUri
import com.tencent.news.core.platform.api.IKtUri
import io.ktor.http.URLBuilder
import io.ktor.http.Url

internal actual fun getPlatformUri(): IAppUri {
    return OhosPlatformUri
}

internal actual fun getPlatformRegex(): IAppRegex = AppPlatformRegex()

object OhosPlatformUri : IAppUri {
    override fun parseUri(uriString: String): IKtUri {
        return OhosKtUri(uriString)
    }
}

class OhosKtUri(val uriString: String) : IKtUri {

    /**
     * Keep Android/iOS-compatible behavior for route ids like "act_ai_chat".
     * Ktor treats relative strings as localhost URLs, so non-standard inputs use
     * the same empty host/query fallback as Android Uri.parse / NSURLComponents.
     */
    private val isStandardUri = uriString.contains("://")

    private val url = Url(uriString)

    override val scheme: String?
        get() = if (isStandardUri) url.protocol.name.takeIf { it.isNotEmpty() } else null

    override val host: String
        get() = if (isStandardUri) url.host else ""

    override val path: String
        get() = if (isStandardUri) {
            url.encodedPath.concatPrefix("/", concatIfEmpty = true)
        } else {
            uriString.concatPrefix("/", concatIfEmpty = true)
        }

    override fun getQuery(key: String): String {
        return if (isStandardUri) url.parameters[key].getNonNull() else ""
    }

    override fun getAllQuery(): Map<String, String> {
        if (!isStandardUri) return emptyMap()
        val result = mutableMapOf<String, String>()
        url.parameters.entries().forEach { (key, values) ->
            result[key] = values.firstOrNull().getNonNull()
        }
        return result
    }

    override fun mutate(): IKtMutableUri {
        return OhosMutableUri(uriString)
    }
}

class OhosMutableUri(private val uriString: String) : IKtMutableUri {

    private val builder = URLBuilder(uriString)
    private val params = mutableMapOf<String, String>()
    private var hasSetup = false

    private fun setupParams() {
        if (hasSetup) return
        hasSetup = true
        builder.parameters.entries().forEach { (key, values) ->
            params[key] = values.firstOrNull().getNonNull()
        }
    }

    override fun appendQuery(key: String, value: String, putIfAbsent: Boolean) {
        setupParams()
        if (!putIfAbsent || params[key].isNullOrEmpty()) {
            params[key] = value
        }
    }

    override fun build(): IKtUri {
        return OhosKtUri(asString())
    }

    override fun asString(): String {
        setupParams()
        builder.parameters.clear()
        params.forEach { (key, value) ->
            builder.parameters.append(key, value)
        }
        return builder.buildString()
    }
}
