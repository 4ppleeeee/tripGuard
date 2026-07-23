package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IRegex
import com.tencent.news.core.platform.api.IRegexResult

/**
 * kt语言实现的正则能力，底层调用还是各端本身的api
 */
class KtRegex(private val pattern: Regex) : IRegex {
    override fun containsMatchIn(input: String): Boolean = pattern.containsMatchIn(input)

    override fun find(input: String): IRegexResult? =
        pattern.find(input)?.let { KtRegexResult(it) }

    override fun findAll(input: String): List<IRegexResult> =
        pattern.findAll(input).map { KtRegexResult(it) }.toList()

    override fun replace(input: String, replacement: String): String =
        pattern.replace(input, replacement)
}

class KtRegexResult(private val matcher: MatchResult) : IRegexResult {
    override val range: IntRange
        get() = IntRange(
            matcher.range.first,
            matcher.range.last + 1
        )

    override fun next(): IRegexResult? {
        return matcher.next()?.let {
            KtRegexResult(it)
        }
    }
}

/**
 * 包装的正则实现类，子类可以根据平台能力的需要自行包装
 */
class AppPlatformRegex : IAppRegex {
    override fun build(regex: String, isIgnoreCase: Boolean): IRegex {
        return KtRegex(
            Regex(
                regex,
                if (isIgnoreCase) {
                    setOf(RegexOption.IGNORE_CASE)
                } else {
                    emptySet()
                }
            )
        )
    }
}