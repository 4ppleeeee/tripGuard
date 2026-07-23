package com.tencent.news.core.platform

import com.tencent.news.core.extension.concatPrefix
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IAppUri
import com.tencent.news.core.platform.api.IKtMutableUri
import com.tencent.news.core.platform.api.IKtUri
import io.ktor.http.Url
import io.ktor.http.URLBuilder

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
     * 是否为标准 URI（包含 "://"）
     * 对于不含 scheme 的纯字符串（如 "act_ai_chat"），ktor 的 Url() 会将其当作相对路径，
     * 并用默认的 http://localhost 补全，导致 host 变成 "localhost"，与 Android/iOS 行为不一致。
     * 此时需要走兜底逻辑，返回与 Android Uri.parse / iOS NSURLComponents 一致的结果。
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

    // 返回值不需要再urlDecode，ktor的parameters已自动解码
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

    // value不需要urlEncode，URLBuilder内部会处理
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
        // 清空原有参数，用合并后的params重新设置
        builder.parameters.clear()
        params.forEach { (key, value) ->
            builder.parameters.append(key, value)
        }
        return builder.buildString()
    }
}
