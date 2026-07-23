package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.list.model.BaseKmmModel
import com.tencent.news.core.list.model.JsonToObjSerializer
import kotlinx.serialization.Serializable


object AdFormComponentType {
    const val TEL = 1       // 电话
    const val FORM = 2      // 外显表单
    const val CONSULT = 3   // 咨询
}

@Suppress("PrivatePropertyName")

@Serializable
class AdFormComponentInfo : BaseKmmModel() {
    var type = 0                        // 组件类型

    private var h5_url: String = ""     // 组件跳转链接，目前行动按钮使用
    var h5Url: String
        get() = h5_url
        set(value) {
            h5_url = value
        }

    private var btn_text: String = ""   // 按钮文案
    val btnText: String
        get() = btn_text

    @Serializable(with = AdFormComponentContentSerializer::class)
    var content: AdFormComponentContent? = null    // 组件更多信息的json
}


@Serializable
class AdFormComponentContent : BaseKmmModel() {
    var componentType = 0   // 组件类型
    var componentId = 0     // 组件id

    var tel: String = ""    // 电话
}


class AdFormComponentContentSerializer : JsonToObjSerializer<AdFormComponentContent>(
    { AdFormComponentContent.serializer() }
)

private fun IKmmAdOrder?.formComp(): AdFormComponentInfo? = this?.action?.formComponent

// 创意组件-电话
fun IKmmAdOrder?.isTelComponent(): Boolean = formComp()?.type == AdFormComponentType.TEL

fun IKmmAdOrder?.getComponentH5Url(): String = formComp()?.h5Url.getNonNull()

fun IKmmAdOrder?.getComponentTel(): String = formComp()?.content?.tel.getNonNull()

fun IKmmAdOrder?.setComponentTel(tel: String) {
    formComp()?.content?.tel = tel
}

// 创意组件-表单
fun IKmmAdOrder?.isFormComponent(): Boolean = formComp()?.type == AdFormComponentType.FORM

// 创意组件-咨询
fun IKmmAdOrder?.isConsultComponent(): Boolean = formComp()?.type == AdFormComponentType.CONSULT
