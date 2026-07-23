package com.tencent.news.core.platform.api

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.LocaleList
import com.tencent.news.core.platform.api.IAppI18n
import com.tencent.news.core.platform.i18n.StringKey
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Android 端 IAppI18n 实现，从 strings.xml 读取本地化字符串。
 *
 * 性能优化：
 * - languageTag 在内存中缓存，避免每次 resolve 触发 SharedPreferences 读取。
 * - localizedContext 按 languageTag 缓存，避免高频重组路径反复 createConfigurationContext。
 * - resId 按 key 缓存（resId 在包生命周期内稳定），避免反复走反射式 getIdentifier；
 *   未命中（resId == 0）也会缓存，避免对缺失 key 反复查找。
 *
 * 适用于用户中心等带高频滚动/重组的页面。
 */
class AndroidAppI18n(private val context: Context) : IAppI18n {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // 内存中的语言标签缓存，避免每次 resolve 都读 SharedPreferences
    @Volatile
    private var memLanguageTag: String? = null

    // 按 languageTag 缓存 Localized Context；切换语言时清空
    @Volatile
    private var cachedLocalizedContext: Context? = null

    @Volatile
    private var cachedContextLanguageTag: String? = null

    // key -> resId 缓存；resId == 0 表示未找到，同样缓存以避免反复 getIdentifier
    private val resIdCache = ConcurrentHashMap<String, Int>()

    override fun currentLanguageTag(): String {
        memLanguageTag?.let { return it }
        val tag = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE_TAG) ?: DEFAULT_LANGUAGE_TAG
        memLanguageTag = tag
        return tag
    }

    override fun setLanguage(languageTag: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageTag).apply()
        memLanguageTag = languageTag
        // 语言切换后失效 localizedContext 缓存；resId 与语言无关，无需清空
        cachedLocalizedContext = null
        cachedContextLanguageTag = null
    }

    override fun resolve(key: String, args: List<Any?>, fallback: String?): String {
        val resId = resolveResId(key)
        if (resId != 0) {
            val localizedContext = getLocalizedContext()
            return if (args.isEmpty()) {
                localizedContext.getString(resId)
            } else {
                getStringWithArgs(localizedContext, resId, args)
            }
        }
        // strings.xml 中缺失 key 时，先回退到 KMM 内的中文兜底文案，最后才落到 fallback / key
        val zhTemplate = StringKey.zhDefaultOf(key)
        if (zhTemplate != null) {
            return if (args.isEmpty()) zhTemplate else formatTemplate(zhTemplate, args)
        }
        return fallback ?: key
    }

    /**
     * 使用 Android Resources 的 [Context.getString] 进行 locale 感知的格式化。
     *
     * Java vararg API（`Object...`）只能通过 Kotlin 的 spread 操作符调用，spread 会触发
     * 一次防御性数组拷贝。这里把不可避免的 spread 收进 helper 并 [Suppress("SpreadOperator")]，
     * 让调用方保持简洁、规则告警只集中在这一处。
     */
    @Suppress("SpreadOperator")
    private fun getStringWithArgs(ctx: Context, resId: Int, args: List<Any?>): String =
        ctx.getString(resId, *args.toTypedArray())

    /**
     * 使用 [String.format] 对 KMM 中的 zhDefault 模板做朴素格式化，理由同 [getStringWithArgs]。
     */
    @Suppress("SpreadOperator")
    private fun formatTemplate(template: String, args: List<Any?>): String =
        String.format(template, *args.toTypedArray())

    override fun resolvePlural(key: String, count: Int, args: List<Any?>, fallback: String?): String {
        return resolve(key, args, fallback)
    }

    override fun shouldUseInternationalVersion(): Boolean = false

    override fun subscribeLanguageChanged(onChanged: (languageTag: String) -> Unit) {
        // TODO: 实现语言切换监听
    }

    private fun resolveResId(key: String): Int {
        val cached = resIdCache[key]
        if (cached != null) return cached
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        resIdCache[key] = resId
        return resId
    }

    private fun getLocalizedContext(): Context {
        val languageTag = currentLanguageTag()
        val cached = cachedLocalizedContext
        if (cached != null && cachedContextLanguageTag == languageTag) {
            return cached
        }
        val locale = Locale.forLanguageTag(languageTag)
        val config = Configuration(context.resources.configuration)
        config.setLocales(LocaleList(locale))
        val newContext = context.createConfigurationContext(config)
        cachedLocalizedContext = newContext
        cachedContextLanguageTag = languageTag
        return newContext
    }

    companion object {
        private const val PREF_NAME = "i18n_prefs"
        private const val KEY_LANGUAGE = "language"
        private const val DEFAULT_LANGUAGE_TAG = "zh-Hans"
    }
}
