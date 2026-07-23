package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IRegex
import com.tencent.news.core.platform.api.IRegexResult
import com.tencent.news.core.platform.api.getShiplySwitch
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE

/**
 * 正则表达式的Android平台实现
 * 由于Android的正则表达式实现与Kotlin的实现有些许性能差异，
 * 为了保证性能，我们需要对其进行封装，以便于在不同平台上进行调用
 */
actual fun getPlatformRegex(): IAppRegex = AppRegex()

class AppRegex : IAppRegex {
    override fun build(regex: String, isIgnoreCase: Boolean): IRegex {
        return if (getShiplySwitch("is_qn_core_use_android_regex", true)) {
            AndroidRegex(
                if (isIgnoreCase) {
                    Pattern.compile(regex, CASE_INSENSITIVE)
                } else {
                    Pattern.compile(regex)
                }
            )
        } else {
            AppPlatformRegex().build(regex, isIgnoreCase)
        }
    }

    /**
     * 使用了安卓平台的一套正则处理方式
     */
    class AndroidRegex(private val pattern: Pattern) : IRegex {
        override fun containsMatchIn(input: String): Boolean =
            pattern.matcher(input).find()

        override fun find(input: String): IRegexResult? {
            val matcher = pattern.matcher(input)
            return if (matcher.find()) {
                AndroidRegexResult(matcher)
            } else {
                return null
            }
        }

        override fun findAll(input: String): List<IRegexResult> {
            val matcher = pattern.matcher(input)
            val list = mutableListOf<IRegexResult>()
            while (matcher.find()) {
                list.add(AndroidRegexResult(matcher))
            }
            return list
        }

        override fun replace(input: String, replacement: String): String =
            pattern.matcher(input).replaceAll(replacement)
    }

    class AndroidRegexResult(private val matcher: Matcher) : IRegexResult {
        override val range: IntRange = IntRange(matcher.start(), matcher.end())

        override fun next(): IRegexResult? {
            return if (matcher.find()) {
                AndroidRegexResult(matcher)
            } else {
                return null
            }
        }
    }
}