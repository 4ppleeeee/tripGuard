package com.tencent.news.core.list.api

import com.tencent.news.core.extension.PrimitiveMap
import com.tencent.news.core.extension.noneNullMap
import com.tencent.news.core.extension.safeEncodeToJson
import com.tencent.news.core.tads.vm.IVMHolder


// 工具：用来转换 vm
fun Map<String, IVMHolder<out IExportModelData?>>.toPrimitiveMap(): PrimitiveMap =
    mapValues {
        it.value.createOrGet()?.buildExportPrimitiveMap()
    }.noneNullMap()

// 工具：用来转换 model 类
fun Map<String, IExportModelData>.buildJsonForVM(): String =
    mapValues { it.value.buildExportPrimitiveMap() }.safeEncodeToJson()