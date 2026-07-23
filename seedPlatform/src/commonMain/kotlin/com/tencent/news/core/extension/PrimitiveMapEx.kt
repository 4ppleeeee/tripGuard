package com.tencent.news.core.extension

import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.isDebug

// 基础类型Map，Any可以是基础类型或Map和List（集合里的嵌套元素也要符合标准）
// （这个目前是基于约定，没有强约束，后续可能考虑改成自定义类）
typealias PrimitiveMap = Map<String, Any>

fun PrimitiveMap.appendWithCheck(vararg params: Pair<String, String>): PrimitiveMap {
    if (isDebug()) {
        params.forEach {
            assertDuplicateKey(key = it.first)
        }
    }
    return this + params.toMap()
}

fun PrimitiveMap.assertDuplicateKey(key: String) {
    if (!containsKey(key)) return

    val fromLog = "${this::class.simpleName} buildExportPrimitiveMap"
    val errorLog = "$fromLog 不要占用 $key 这个key"
    debugToast(errorLog)
    throw RuntimeException(errorLog)
}
