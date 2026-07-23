package com.tencent.news.core.compose.platform

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.PageData
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.extension.unSafeDecode
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.platform.api.appReport
import com.tencent.news.core.serializer.KtJson


/**
 * WARNING: 业务侧请直接调用[com.tencent.news.core.compose.scaffold.ComposePage.rememberedPageArgs]
 *
 * 从Compose的[PageData]里解析业务自定义的[IComposePageArgs]
 */
@KmmInternalApi
inline fun <reified T : IComposePageArgs> PageData.parsePageArgs(): T? {
    return parsePageArgs(this.params)
}

inline fun <reified T : IComposePageArgs> parsePageArgs(params: JSONObject): T? {
    val identifier = params.optInt(NTComposePageArgsPool.PAGE_DATA_KEY)
    var args = NTComposePageArgsPool.popPageArgs(identifier) as? T
    if (args != null) {
        return args
    }

    runCatching {
        // 鸿蒙目前仍跨语言，所以传过来的只有json
        if (isHarmonyPlatform()) {
            args = KtJson.unSafeDecode<T>(params.optString("pageArgs"))
        }

        // 创建一个空对象（注意：这个兜底不靠谱，仅适合不需要页面参数的page，其他pageArgs可能解析失败）
        if (args == null) {
            args = KtJson.unSafeDecode<T>("{}")
        }
        return args
    }.getOrElse { error ->
        appReport().reportBugly("页面 ${T::class.simpleName} 解析失败", error)
        return null
    }

}