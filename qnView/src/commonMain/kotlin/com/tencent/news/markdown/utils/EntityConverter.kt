package com.tencent.news.markdown.utils

import org.intellij.markdown.html.entities.Entities

/**
 * Based on: https://github.com/JetBrains/markdown/blob/master/src/commonMain/kotlin/org/intellij/markdown/html/entities/EntityConverter.kt
 * Removed HTML focused escaping by https://github.com/mikepenz/multiplatform-markdown-renderer/pull/222
 */
internal object EntityConverter {
    private const val ESCAPE_ALLOWED_STRING = """!"#\$%&'\(\)\*\+,\-.\/:;<=>\?@\[\\\]\^_`{\|}~"""
    private const val MIN_SUPPLEMENTARY_CODE_POINT = 0x10000
    private const val MAX_CODE_POINT = 0x10FFFF
    private const val HIGH_SURROGATE_START = 0xD800
    private const val LOW_SURROGATE_END = 0xDFFF
    private val REGEX = Regex("""&(?:([a-zA-Z0-9]+)|#([0-9]{1,8})|#[xX]([a-fA-F0-9]{1,8}));|(["&<>])""")
    private val REGEX_ESCAPES = Regex("${REGEX.pattern}|\\\\([$ESCAPE_ALLOWED_STRING])")

    internal fun replaceEntities(
        text: CharSequence,
        processEntities: Boolean,
        processEscapes: Boolean,
    ): String {
        val regex = if (processEscapes) REGEX_ESCAPES else REGEX
        return regex.replace(text) { match ->
            val g = match.groups
            when {
                g.size > 5 && g[5] != null -> g[5]!!.value[0].toString()
                g[4] != null -> match.value
                else -> {
                    val code = when {
                        !processEntities -> null
                        g[1] != null -> Entities.map[match.value]
                        g[2] != null -> g[2]!!.value.toIntOrNull()
                        g[3] != null -> g[3]!!.value.toIntOrNull(16)
                        else -> null
                    }
                    code?.toCodePointString() ?: match.value
                }
            }
        }
    }

    private fun Int.toCodePointString(): String? {
        if (this < 0 || this > MAX_CODE_POINT || this in HIGH_SURROGATE_START..LOW_SURROGATE_END) {
            return null
        }
        if (this < MIN_SUPPLEMENTARY_CODE_POINT) {
            return toChar().toString()
        }
        val value = this - MIN_SUPPLEMENTARY_CODE_POINT
        return buildString {
            append(((value shr 10) + HIGH_SURROGATE_START).toChar())
            append(((value and 0x3FF) + 0xDC00).toChar())
        }
    }
}
