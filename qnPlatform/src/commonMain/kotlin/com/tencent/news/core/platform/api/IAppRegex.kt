package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.getPlatformRegex

/**
 * 正则匹配接口
 * Author: joejhzhou
 * Date: 2025/3/3
 */
interface IAppRegex {
    /**
     * 构建正则处理相关逻辑的包装类
     * @param regex 正则表达式
     * @param isIgnoreCase 是否忽略大小写
     */
    fun build(regex: String, isIgnoreCase: Boolean = true): IRegex
}

/**
 * 各平台对外暴露的正则匹配相关能力
 */
interface IRegex {
    /**
     * 输入的句子有任何一个短语被正则匹配上都返回true
     * @param input 输入的句子
     */
    fun containsMatchIn(input: String): Boolean

    /**
     * 返回下一个正则匹配的结果
     * @param input 输入的句子
     */
    fun find(input: String): IRegexResult?

    /**
     * 返回所有匹配的结果
     * @param input 输入的句子
     */
    fun findAll(input: String): List<IRegexResult>

    /**
     * 替换输入中被正则匹配到的短语为给定的替换短语
     * @param input 输入的句子
     * @param replacement 给定的替换短语
     */
    fun replace(input: String, replacement: String): String
}

interface IRegexResult {
    /**
     * 正则匹配的结果范围
     * first 匹配到的第一个字符的位置，包含
     * last 匹配到的最后一个字符后一位，不包含
     */
    val range: IntRange

    /**
     * 找下一个被正则匹配的结果
     */
    fun next(): IRegexResult?
}

@OptIn(KmmInternalApi::class)
fun appRegex(): IAppRegex {
    /**
     * 各端实现的正则能力
     */
    return QnPlatformLogic.appRegex ?: getPlatformRegex()
}