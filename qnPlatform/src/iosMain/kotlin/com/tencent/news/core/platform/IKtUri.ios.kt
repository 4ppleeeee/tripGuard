package com.tencent.news.core.platform

import com.tencent.news.core.extension.concatPrefix
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.platform.api.IAppUri
import com.tencent.news.core.platform.api.IKtMutableUri
import com.tencent.news.core.platform.api.IKtUri
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem


actual fun getPlatformUri(): IAppUri = IOSPlatformUri

object IOSPlatformUri : IAppUri {
    override fun parseUri(uriString: String): IKtUri = IOSKtUri(uriString)
}

class IOSKtUri(uriString: String) : IKtUri, IKtMutableUri {

    private val uri = NSURLComponents(uriString)
    override val scheme: String?
        get() = uri.scheme

    override val host: String
        get() = uri.host.getNonNull()

    override val path: String
        get() = uri.path.getNonNull().concatPrefix("/", concatIfEmpty = true)

    override fun mutate(): IKtMutableUri {
        setupParams()
        return this
    }

    private val params = mutableMapOf<String, String>()
    private var hasSetup = false

    private fun setupParams() {
        if (hasSetup) {
            return
        }
        hasSetup = true
        uri.queryItems?.mapNotNull {
            it as? NSURLQueryItem
        }?.forEach {
            params[it.name] = it.value.getNonNull()
        }
    }

    override fun appendQuery(key: String, value: String, putIfAbsent: Boolean) {
        if (!putIfAbsent || (params[key].isNullOrEmpty())) {
            params[key] = value
        }
    }

    override fun getQuery(key: String): String {
        return uri.queryItems?.filterIsInstance<NSURLQueryItem>()
            ?.find { it.name == key }?.value.getNonNull()
    }

    override fun getAllQuery(): Map<String, String> {
        setupParams()
        return params
    }

    override fun build(): IKtUri {
        fillQuery()
        return this
    }

    override fun asString(): String {
        fillQuery()
        return uri.string.getNonNull()
    }

    private fun fillQuery() {
        if (params.isEmpty()) {
            uri.queryItems = null // 不设置queryItems，避免添加"?"
        } else {
            uri.queryItems = params.mapTo(ArrayList()) {
                NSURLQueryItem(it.key, it.value)
            }
        }
    }
}