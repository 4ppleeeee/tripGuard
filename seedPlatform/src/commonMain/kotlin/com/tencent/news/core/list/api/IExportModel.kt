package com.tencent.news.core.list.api

import com.tencent.news.core.extension.IOhosExportDoc
import com.tencent.news.core.extension.PrimitiveMap
import com.tencent.news.core.extension.appendWithCheck
import com.tencent.news.core.list.constants.ExportModelType


// todo 【架构说明】：导出给鸿蒙使用的协议，注意必须都是基础数据类型（可以嵌套List或Map）
interface IExportModel : IExportModelData {

    val exportModelType: ExportModelType
    val exportUniqueKey: String

    override fun buildExportPrimitiveMap(): PrimitiveMap // Any里都得是基础类型，否则鸿蒙不认识

    companion object {
        // buildExportPrimitiveMap 用到的一些通用key值：
        // （业务私参不要在这加，子类自己定）

        // 所有对鸿蒙暴露的model，统一的2个key：
        const val KEY_MODEL_TYPE = "export_model_type"
        const val KEY_UNIQUE_KEY = "export_unique_key"

        const val KEY_JSON = "origin_json"  // 原始json，目前暂且暴露原始model，后面往vm里切换
        const val KEY_VM = "export_vm"      // UI层可直接使用的vm，期望后续只给鸿蒙暴露vm

        // appEventBus() 给鸿蒙发送广播事件使用
        const val KEY_EVENT_NAME = "export_event_name"
        const val KEY_EVENT_DATA = "export_event_data"
    }

}

// 带类型和唯一key，可区分的PrimitiveMap
fun IExportModel.buildDistinguishableExportMap(): PrimitiveMap {
    return buildExportPrimitiveMap().appendWithCheck(
        IExportModel.KEY_MODEL_TYPE to exportModelType.key,
        IExportModel.KEY_UNIQUE_KEY to exportUniqueKey,
    )
}

interface IExportModelData : IOhosExportDoc {
    fun buildExportPrimitiveMap(): PrimitiveMap // Any里都得是基础类型，否则鸿蒙不认识
}
