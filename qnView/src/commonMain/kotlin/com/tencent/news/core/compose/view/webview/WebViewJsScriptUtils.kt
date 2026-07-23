package com.tencent.news.core.compose.view.webview

/**
 * WebView 注入脚本工具。
 */
object WebViewJsScriptUtils {

    /**
     * 转义 JS 字符串中的特殊字符（单次遍历，O(n) 时间复杂度）。
     *
     * 覆盖：反斜杠、引号、换行符、Unicode 行/段分隔符、null 字符、script 标签。
     */
    fun escapeJsString(str: String): String {
        val len = str.length
        val sb = StringBuilder(len + (len shr 3) + 16)
        var i = 0
        while (i < len) {
            val c = str[i]
            when (c) {
                '\\' -> sb.append("\\\\")
                '\'' -> sb.append("\\'")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\u2028' -> sb.append("\\u2028")
                '\u2029' -> sb.append("\\u2029")
                '\u0000' -> sb.append("\\0")
                '<' -> {
                    if (i + 8 < len &&
                        str[i + 1] == '/' &&
                        str.regionMatches(i + 2, "script>", 0, 7, ignoreCase = true)
                    ) {
                        sb.append("<\\/script>")
                        i += 9
                        continue
                    }
                    sb.append(c)
                }
                else -> sb.append(c)
            }
            i++
        }
        return sb.toString()
    }
}
