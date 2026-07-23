package com.tencent.kmm.demo

import com.tencent.news.core.compose.platform.IComposePageArgs
import org.json.JSONObject

/**
 * 用于 Kuikly DSL 页面通过 KRRouterAdapter.openPage 启动时传递原始 pageData。
 *
 * 当 [KuiklyRenderActivity] 从 Intent 的 "pageData" extra 中读取到 JSON 字符串时，
 * 构造此对象。其 [pushPageArgsToMap] 返回从 JSON 解析出的完整 map，
 * 使 Kuikly 渲染引擎能将这些参数传递到 DSL 页面的 pagerData.params 中。
 */
@Suppress("UNCHECKED_CAST")
class DslPageDataArgs(jsonStr: String) : IComposePageArgs {

    private val dataMap: Map<String, Any>

    init {
        val json = JSONObject(jsonStr)
        val map = mutableMapOf<String, Any>()
        json.keys().forEach { key ->
            val value = json.get(key)
            // JSONObject 需要转为嵌套 Map，以便 Kuikly 引擎正确解析
            when (value) {
                is JSONObject -> {
                    val innerMap = mutableMapOf<String, Any>()
                    value.keys().forEach { innerKey ->
                        innerMap[innerKey] = value.get(innerKey)
                    }
                    map[key] = innerMap
                }
                else -> map[key] = value
            }
        }
        dataMap = map
    }

    /**
     * 返回包含原始 pageData 所有字段的 map。
     * 注意：接口声明为 Map<String, Int>，但 Kuikly SDK 的 onAttach 实际接受 Map<String, Any>，
     * 这里通过类型擦除传递完整的参数 map。
     */
    override val pushPageArgsToMap: Map<String, Int>
        get() = dataMap as Map<String, Int>

    override val identifier: Int
        get() = dataMap.hashCode()
}
