package com.tencent.kmm.demo

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Locale

internal enum class SourcePlatform(
    val displayName: String,
    val shortStatus: String,
    val description: String,
    val accentColor: Int,
    val inputHint: String,
) {
    XIAOHONGSHU(
        displayName = "小红书",
        shortStatus = "移动 H5 已跑通",
        description = "分享笔记链接，解析标题、正文和封面。",
        accentColor = 0xFFE64566.toInt(),
        inputHint = "粘贴小红书分享文案或 URL",
    ),
    MAFENGWO(
        displayName = "马蜂窝",
        shortStatus = "待验证",
        description = "粘贴游记、攻略或目的地链接，先走通用网页解析。",
        accentColor = 0xFFFF9F1C.toInt(),
        inputHint = "粘贴马蜂窝游记、攻略或目的地 URL",
    ),
    WEB(
        displayName = "通用网页",
        shortStatus = "兜底解析",
        description = "用网页 meta/title/image 兜底，适合快速试新平台。",
        accentColor = 0xFF64748B.toInt(),
        inputHint = "粘贴任意网页 URL",
    ),
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
        } else if (
            lower.contains("mafengwo.cn") ||
            lower.contains("imfw.cn") ||
            lower.contains("马蜂窝")
        ) {
            SourcePlatform.MAFENGWO
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
