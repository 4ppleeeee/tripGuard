package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.i18n.StringKey

interface IAppI18n {

    /**
     * 当前语言标签，例如 "zh-Hans", "en", "zh-Hant"
     */
    fun currentLanguageTag(): String

    /**
     * 切换语言
     */
    fun setLanguage(languageTag: String)

    /**
     * 根据 key 解析本地化字符串
     *
     * @param key 资源 key，对应 StringKey 的 name
     * @param args 格式化参数，按顺序替换 Native 资源中的占位符
     * @param fallback key 无法解析时的回退文案
     * @return 解析后的最终文案
     */
    fun resolve(
        key: String,
        args: List<Any?> = emptyList(),
        fallback: String? = null
    ): String

    /**
     * 根据 key 解析带复数的本地化字符串
     *
     * @param key 资源 key
     * @param count 数量，用于复数规则判定
     * @param args 格式化参数
     * @param fallback key 无法解析时的回退文案
     * @return 解析后的最终文案
     */
    fun resolvePlural(
        key: String,
        count: Int,
        args: List<Any?> = emptyList(),
        fallback: String? = null
    ): String

    /**
     * 当前是否处于国际版内容态：i18n 总开关开启 + 英文环境 + 自动翻译开启。
     */
    fun shouldUseInternationalVersion(): Boolean = false

    /**
     * 订阅语言切换事件
     * 每次语言变化时，onChanged 会被调用，参数为新的语言标签
     */
    fun subscribeLanguageChanged(onChanged: (languageTag: String) -> Unit)
}

/**
 * [IAppI18n] 实例持有者。由宿主在启动时注入，未注入时返回 [DefaultAppI18n]。
 */
object AppI18nHolder {
    private var _instance: IAppI18n? = null

    fun set(i18n: IAppI18n) {
        _instance = i18n
    }

    fun get(): IAppI18n = _instance ?: DefaultAppI18n
}

/**
 * 获取已注入的 [IAppI18n] 实例，若宿主未注入则返回 [DefaultAppI18n]。
 */
fun appI18n(): IAppI18n = AppI18nHolder.get()

/**
 * 默认实现：宿主未注入 i18n 能力时的降级。
 *
 * 解析顺序：
 * 1. 优先使用 [StringKey.zhDefault] 中登记的中文兜底文案；
 * 2. 否则使用调用方传入的 [fallback]；
 * 3. 兜底返回 key 本身（保证调试可见）。
 *
 * 因此 Android / 鸿蒙等未注入 [IAppI18n] 的宿主，UI 上仍会展示正常的中文文案，
 * 不会出现 `USER_CENTER_MESSAGE` 这类原始 key 漏出。
 */
private object DefaultAppI18n : IAppI18n {
    private var _languageTag: String = "zh-Hans"

    override fun currentLanguageTag(): String = _languageTag

    override fun setLanguage(languageTag: String) {
        _languageTag = languageTag
    }

    override fun resolve(key: String, args: List<Any?>, fallback: String?): String {
        val template = StringKey.zhDefaultOf(key) ?: fallback ?: key
        return applyArgs(template, args)
    }

    override fun resolvePlural(key: String, count: Int, args: List<Any?>, fallback: String?): String {
        // 中文不区分复数形式，等价于 resolve
        return resolve(key, args, fallback)
    }

    override fun subscribeLanguageChanged(onChanged: (languageTag: String) -> Unit) = Unit

    /**
     * 朴素的 %s 占位符替换，按顺序消费 [args]。
     * 仅用于 KMM 内默认兜底，不追求与 Java [String.format] 完全等价。
     */
    private fun applyArgs(template: String, args: List<Any?>): String {
        if (args.isEmpty()) return template
        var result = template
        for (arg in args) {
            val idx = result.indexOf("%s")
            if (idx < 0) break
            result = result.substring(0, idx) + (arg?.toString() ?: "") + result.substring(idx + 2)
        }
        return result
    }
}
