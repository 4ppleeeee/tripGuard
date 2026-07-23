package com.tencent.news.core.compose.view

import com.tencent.news.core.platform.api.appStorage
import com.tencent.news.core.platform.api.appUri
import com.tencent.news.core.platform.api.getTable

object ThirdPartyLinkHelper {
    private const val STORAGE_TABLE = "third_party_link_policy"
    private const val KEY_SKIP_CONFIRM = "skip_confirm"

    private val trustedDomains = listOf(
        ".qq.com"
    )

    private val table by lazy { appStorage().getTable(STORAGE_TABLE) }

    private fun isThirdPartyLink(originUrl: String): Boolean {
        val host = runCatching { appUri().parseUri(originUrl).host.lowercase() }
            .getOrNull().orEmpty()
        if (host.isEmpty()) {
            // 解析失败的话开头是http的都认为第三方链接出弹窗
            val looksLikeHttp = originUrl.startsWith("http://", ignoreCase = true) ||
                originUrl.startsWith("https://", ignoreCase = true)
            return looksLikeHttp
        }
        return trustedDomains.none { host.contains(it) }
    }

    fun shouldSkipConfirm(): Boolean = table.getBoolean(KEY_SKIP_CONFIRM, false)

    fun updateSkipConfirm(skip: Boolean) {
        table.putBoolean(KEY_SKIP_CONFIRM, skip)
    }

    fun shouldShowConfirm(originUrl: String): Boolean {
        return isThirdPartyLink(originUrl) && !shouldSkipConfirm()
    }
}
