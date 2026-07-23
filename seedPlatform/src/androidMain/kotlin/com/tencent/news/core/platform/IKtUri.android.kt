package com.tencent.news.core.platform

import android.net.Uri
import com.tencent.news.core.extension.concatPrefix
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.platform.api.IAppUri
import com.tencent.news.core.platform.api.IKtMutableUri
import com.tencent.news.core.platform.api.IKtUri


actual fun getPlatformUri(): IAppUri = AndroidPlatformUri

object AndroidPlatformUri : IAppUri {
    override fun parseUri(uriString: String): IKtUri = AndroidKtUri(Uri.parse(uriString))
}

class AndroidKtUri(private val uri: Uri) : IKtUri, IKtMutableUri {
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

    private val builder by lazy { uri.buildUpon() }
    private val params = mutableMapOf<String, String>()
    private var hasSetup = false

    private fun setupParams() {
        if (uri.isOpaque || hasSetup) {
            return
        }
        hasSetup = true
        builder.clearQuery()
        uri.queryParameterNames.forEach {
            params[it] = getQuery(it)
        }
    }

    override fun appendQuery(key: String, value: String, putIfAbsent: Boolean) {
        if (!putIfAbsent || (uri.isOpaque || getQuery(key).isEmpty())) {
            params[key] = value
        }
    }

    override fun getQuery(key: String): String = uri.getQueryParameter(key).getNonNull()

    override fun getAllQuery(): Map<String, String> {
        setupParams()
        return params
    }

    override fun build(): IKtUri = AndroidKtUri(builder.fillQuery().build())

    override fun asString(): String = builder.fillQuery().toString()

    private fun Uri.Builder.fillQuery(): Uri.Builder {
        clearQuery()
        params.forEach {
            appendQueryParameter(it.key, it.value)
        }
        return this
    }

}