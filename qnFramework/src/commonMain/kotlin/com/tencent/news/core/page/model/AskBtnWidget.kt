package com.tencent.news.core.page.model


import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
@SerialName(StructWidgetType.ASK_BTN)
class AskBtnWidget : StructBtnWidget<AskBtnWidgetData>(), IKmmKeep {

    @Serializable(AskBtnWidgetDataWrapperSerializer::class)
    override var data: AskBtnWidgetData? = null

    // 点击逻辑（Shiply 配置、scheme 组装、路由）下沉到 VM/数据层，由构建方（qnDetail）注入 IAskBtnVM。
    // 类型用框架层的 IStructWidgetVM，具体实现在上层模块，渲染侧按 IAskBtnVM 分发。
    @Transient
    @kotlin.jvm.Transient
    override var asWidgetVM: IStructWidgetVM? = null

    override fun getWidgetType() = StructWidgetType.ASK_BTN

}

@Serializable
class AskBtnWidgetData : StructBtnWidgetData(), IKmmKeep {

    @SerialName("event_id")
    var eventId: String = ""

    var hint: String = ""       // 默认文案

}

class AskBtnWidgetDataWrapperSerializer : DataWrapperSerializer<AskBtnWidgetData>(
    StructWidgetType.ASK_BTN, AskBtnWidgetData.serializer()
)