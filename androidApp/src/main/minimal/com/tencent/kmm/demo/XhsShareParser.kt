package com.tencent.kmm.demo

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

internal enum class SourcePlatform(val displayName: String) {
    XIAOHONGSHU("小红书"),
    WEB("网页"),
}

internal object XhsShareParser {

    fun extractFirstUrl(rawText: String): String? =
        Regex("(https?://[^\\s，。；、]+)", RegexOption.IGNORE_CASE)
            .find(rawText)
            ?.value
            ?.trimEnd('.', ',', ';', ')', ']', '"', '\'')

    fun detectPlatform(text: String): SourcePlatform {
        val lower = text.lowercase(Locale.ROOT)
        return if (
            lower.contains("xiaohongshu.com") ||
            lower.contains("xhslink.cn") ||
            lower.contains("xhslink.com") ||
            lower.contains("xhscdn.com") ||
            lower.contains("小红书")
        ) {
            SourcePlatform.XIAOHONGSHU
        } else {
            SourcePlatform.WEB
        }
    }

    fun normalizeResolvedUrl(url: String): String {
        val redirectPath = Regex("[?&]redirectPath=([^&#]+)").find(url)?.groupValues?.getOrNull(1)
            ?: return url
        return URLDecoder.decode(redirectPath, StandardCharsets.UTF_8.name())
    }

    fun isXhsLoginRedirect(url: String): Boolean {
        val lower = url.lowercase(Locale.ROOT)
        return lower.contains("xiaohongshu.com/login") && lower.contains("redirectpath=")
    }

    fun extractNoteId(url: String): String? =
        Regex("/(?:explore|(?:discovery/)?item)/([0-9a-fA-F]+)").find(url)?.groupValues?.getOrNull(1)

    fun isXhsNoteUrl(url: String): Boolean =
        detectPlatform(url) == SourcePlatform.XIAOHONGSHU && extractNoteId(url) != null

    fun deriveTitle(rawText: String): String? {
        val beforeUrl = rawText.substringBefore("http").trim()
        val cleaned = beforeUrl
            .replace(Regex("\\s+"), " ")
            .removeSuffix("打开【小红书】，这篇笔记值得一看~")
            .trim(' ', '，', ',', '。')
        return cleaned.takeIf { it.isNotBlank() }?.compact(48)
    }

    fun deriveFallbackDescription(rawText: String, resolvedUrl: String): String {
        val noteId = extractNoteId(resolvedUrl)
        val source = if (rawText.contains("打开【小红书】")) "分享文案" else "链接"
        return if (noteId.isNullOrBlank()) {
            "已保存小红书$source，平台正文需要登录态或平台适配器继续解析。"
        } else {
            "已展开小红书笔记 $noteId，平台正文需要登录态或平台适配器继续解析。"
        }
    }

    fun selectDescription(
        platform: SourcePlatform,
        parsedDescription: String?,
        rawText: String,
        resolvedUrl: String,
    ): String? {
        parsedDescription?.takeIf { it.isNotBlank() }?.let { return it }
        return if (platform == SourcePlatform.XIAOHONGSHU) {
            deriveFallbackDescription(rawText, resolvedUrl)
        } else {
            null
        }
    }

    fun isUsefulParsedTitle(title: String?): Boolean {
        val normalized = title?.trim().orEmpty()
        if (normalized.isBlank()) {
            return false
        }
        return normalized != "小红书 - 你的生活兴趣社区"
    }
}
