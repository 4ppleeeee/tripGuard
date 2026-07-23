@file:Suppress("PropertyName", "unused", "VariableNaming")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure
import com.tencent.news.core.extension.IStructWidgetDoc
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.safeAddAll
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.ChannelWidgetAction.Companion.createChannelWidget
import com.tencent.news.core.platform.api.debugToast
import com.tencent.news.core.platform.api.isDebug
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.tag.model.IKmmTagInfo
import com.tencent.news.core.user.model.IUserInfo
import com.tencent.news.qnchannel.api.IChannelInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject


typealias WidgetCondition = (StructWidget) -> Boolean

/**
 * 可用于查找整个widget树中的节点
 */

interface IWidgetProvider {

    // 整个widget树的根widget
    val rootWidget: StructPageWidget

    // 用于携带一些上下文信息，供业务逻辑判断用
    val widgetEnv: IWidgetEnv?

    // 查找整个widget树内的任意widget
    fun findWidget(condition: WidgetCondition): List<StructWidget>

    // 用于根据layout构建组件列表
    fun buildWidgetsByRef(vararg widgetRefList: StructWidgetRef?): List<StructWidget>

}

fun IWidgetProvider.findWidgetById(widgetId: String?): StructWidget? =
    findWidget { it.widget_id == widgetId }.firstOrNull()


interface IWidgetEnv {

    fun createDefaultChannelWidget(): ChannelWidget?

    fun getNewsChannel(): String

    // todo 架构说明：此处只允许加新闻内容介质的数据结构，不要随便加业务字段

    val pageArticle: IKmmFeedsItem?

    var userInfo: IUserInfo?

    var tagInfo: IKmmTagInfo?

}

data class ChannelWidgetEnv(
    val channelInfo: IChannelInfo,
) : IWidgetEnv {
    override fun createDefaultChannelWidget() = channelInfo.createChannelWidget()
    override fun getNewsChannel(): String = channelInfo.channelKey
    override val pageArticle: IKmmFeedsItem? = null
    override var userInfo: IUserInfo? = null
    override var tagInfo: IKmmTagInfo? = null
}

interface IWidgetParent<Layout : StructWidgetLayout> {
    fun buildLayoutWidgets(layout: Layout?)
    fun getSubWidgets(): List<StructWidget>?
}

/**
 * 可在item列表中进行展示的widget，需要实现该接口（常用的有 卡片、列表 类型的）
 */
interface IFeedsItemWidget {
    fun getItemWidgets(): List<IKmmFeedsItem>
}

fun List<StructWidget>.toFeedsItemList(): List<IKmmFeedsItem> {
    return flatMap {
        safeList((it as? IFeedsItemWidget)?.getItemWidgets())
    }
}

interface IStructLayoutBinder {
    fun bindWithWidget(provider: IWidgetProvider)
}

// 目前仅header组件支持了，其他组件需要的话类比着加
interface IStructWidgetAware {
    fun onInjectStructWidget(widget: StructWidget)
}

// ktx多态解析讲解： https://medium.com/@veeresh.charantimath8/handling-polymorphic-response-using-kotlinx-f22b507bb84e

@Polymorphic // 【注意】类必须声明 abstract，否则 ktx 解析时无法响应多态
@Serializable
abstract class StructWidget : IKmmPure, IKmmKeep, IStructWidgetDoc {

    // 【注意】这里不要定义 widget_type 字段，会与 ktx 的多态解析 key 冲突；
    //   这个 class 本身就代表了组件类型，尽量不再依赖 widget_type 字段
    //   var widget_type: String = ""

    var widget_id: String = ""
        get() {
            if (field.isBlank()) {
                field = "${getWidgetType()}_${hashCode()}_default"
            }
            return field
        }

    // 【坑】不能用private，编译器会报内部错误：Symbol with IrPropertySymbolImpl is unbound
    internal var show_type: Int = 0
    var showType: Int // 见：StructWidgetShowType
        get() = show_type
        set(value) {
            show_type = value
        }

    // todo 【架构说明】：所有widget通用vm，后续组件注册根据vm来区分，逐渐隐藏widget
    @Transient
    open val asWidgetVM: IStructWidgetVM? = null

    @Transient
    private var widgetProvider: IWidgetProvider? = null

    abstract fun getWidgetType(): String

    open fun onDataPreload() {
    }

    fun bindWidgetProvider(widgetProvider: IWidgetProvider) {
        this.widgetProvider = widgetProvider
    }

    open fun wp(): IWidgetProvider? {
        if (widgetProvider == null && isDebug()) {
            debugToast("【警告】${this::class.simpleName} wp为空！请检查组件数据绑定")
        }
        return widgetProvider
    }

    fun getWidgetEnv(): IWidgetEnv? = widgetProvider?.widgetEnv

    // 查找根组件
    fun findStructPageWidget(): StructPageWidget? = wp()?.rootWidget
    fun findStructPageWidget2(): StructPageWidget2? = wp()?.rootWidget as? StructPageWidget2

    override fun toString(): String = "【${this::class.simpleName}|${widget_id}】"
}

abstract class StructSimpleWidget() : StructWidget() {
    override fun getWidgetType() = StructWidgetType.SIMPLE_WIDGET
}

abstract class StructVMWidget<T : IStructWidgetVM>() : StructWidget() {
    override fun getWidgetType() = StructWidgetType.VM_WRAPPER
    abstract override val asWidgetVM: T
}

object StructWidgetEx {

    /**
     * 解析 [StructWidgetRef] 对应的组件，包括2种情况：
     * - 如果是通过 widget_id 指定的索引，则产出1个对应组件（常用于上游直接配置，静态的组件）
     * - 如果是通过 group_id 指定的索引，可能产出一组组件（常用于这部分组件是由算法动态生成，个数不固定的）
     */
    inline fun <reified T : StructWidget> StructWidget.buildWidgetList(
        refList: List<StructWidgetRef>?,
    ): MutableList<T> {
        val result = mutableListOf<T>()

        val wp = wp()
            ?: return result

        refList?.forEach { ref ->
            result.safeAddAll(wp.buildWidgetsByRef(ref).filterIsInstance<T>())
        }

        result.forEach { it.bindWidgetProvider(wp) }

        return result
    }

    inline fun <reified T : StructWidget> StructWidget.buildSingleWidget(
        ref: StructWidgetRef?,
    ): T? = buildWidgetList<T>(safeList(ref)).firstOrNull()

    inline fun <reified T : StructWidget> StructWidget.buildSingleWidget(
        widget: T,
        noinline builder: (T.() -> Unit)? = null,
    ): T {
        wp()?.let { widget.bindWidgetProvider(it) }
        builder?.invoke(widget)
        return widget
    }

    inline fun <reified T : StructWidget> StructWidget.findSingleWidget(
        noinline condition: WidgetCondition? = null,
    ): T? = findWidgetList<T>(condition)?.firstOrNull()

    // 查找某个组件的vm：互相调用的时候这个很常用
    inline fun <reified T : IStructWidgetVM> StructWidget.findSingleWidgetVM(
        noinline condition: WidgetCondition? = null,
    ): T? = wp()?.findWidget { it.asWidgetVM is T }?.firstOrNull()?.asWidgetVM as? T

    inline fun <reified T : StructWidget> StructWidget.findWidgetList(
        noinline condition: WidgetCondition? = null,
    ): List<T>? {
        val finalCondition = condition ?: { it is T }
        return wp()?.findWidget(finalCondition)?.filterIsInstance<T>()
    }

    fun StructWidget.findPageBusinessType(): String? =
        findStructPageWidget()?.data?.business_type

}

@Serializable
open class StructWidgetLayout : StructWidgetRef() {
    // 预留
}

@Serializable
open class StructWidgetData : IKmmPure, IKmmKeep {
    // 预留
}

// 用于解析组件 data（会扒掉 data.${widget_type} 这一层节点）
open class DataWrapperSerializer<T>(
    private val widgetType: String,
    private val realSerializer: KSerializer<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor = realSerializer.descriptor

    override fun deserialize(decoder: Decoder): T {
        val dataWrapper = decoder.decodeSerializableValue(JsonObject.serializer())

        val data = dataWrapper[widgetType]
            ?: return KtJson.decodeFromString(realSerializer, "{}")

        return KtJson.decodeFromJsonElement(realSerializer, data)
    }

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeSerializableValue(realSerializer, value)
    }
}

@Serializable
open class StructWidgetRef : IKmmPure, IKmmKeep {
    var widget_id: String = ""
    var group_id: String = ""

    var widgetId: String
        get() = widget_id
        set(value) {
            widget_id = value
        }

    var groupId: String
        get() = group_id
        set(value) {
            group_id = value
        }

    override fun toString() =
        if (group_id.isNotNullOrEmpty()) {
            "(group_id=$group_id)"
        } else {
            "(widget_id=$widget_id)"
        }

}

@Serializable
class StructWidgetList : IKmmPure, IKmmKeep {
    var widget_list: List<StructWidgetRef>? = null
}

@Serializable
open class StructWidgetAction : IKmmPure, IKmmKeep {
    // 预留
}

interface IStructWidgetVM

interface StructWidgetViewModel : IStructWidgetVM {
    fun onDisposed() {}
}

internal class StructWidgetHolder(pageWidget: StructPageWidget) : IWidgetProvider {

    override var widgetEnv: IWidgetEnv? = null

    internal var widgetList: List<StructWidget>? = null

    internal var widgetGroup: Map<String, StructWidgetList>? = null

    override var rootWidget: StructPageWidget = pageWidget

    override fun findWidget(condition: (StructWidget) -> Boolean): List<StructWidget> {
        val pageWidgetList = rootWidget.findWidgetInPageWidgetTree(condition)
        val layoutWidgetList = widgetList?.filter { condition(it) }

        val result = LinkedHashSet<StructWidget>() // 用来排重
        result.safeAddAll(pageWidgetList)
        result.safeAddAll(layoutWidgetList)

        return result.toList()
    }

    override fun buildWidgetsByRef(vararg widgetRefList: StructWidgetRef?): List<StructWidget> {
        val result = mutableListOf<StructWidget>()

        widgetRefList.filterNotNull().forEach { widgetRef ->
            val widgetId = widgetRef.widget_id
            val groupId = widgetRef.group_id

            if (widgetId.isNotNullOrEmpty()) {
                val widget = findWidgetById(widgetId)
                widget?.let { result.add(it) }
            }

            if (groupId.isNotNullOrEmpty()) {
                widgetGroup?.get(groupId)?.widget_list?.forEach { ref ->
                    val widget = findWidgetById(ref.widget_id)
                    if (isSupportChannel(widget)) {
                        widget?.let { result.add(it) }
                    }
                }
            }
        }

        return result
    }

    // 需要过滤废弃频道的话在这里做
    private fun isSupportChannel(widget: StructWidget?): Boolean {
        val channelShowType = (widget as? ChannelWidget)?.data?.channel_info?.channelShowType
            ?: return true
        return channelShowType != 114 // GuestOtherFragment，接口：getExtraIconList
    }

}