@file:Suppress("PropertyName", "VariableNaming")

package com.tencent.news.core.page.model


import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.extension.safeSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface IWidgetChannelBarVM { // 预留的，暂时没用；还是在compose里用的state
    val items: List<ChannelBarItem>
    val selectedIndex: StateFlow<Int>
    fun changeSelection(index: Int)
}

@Serializable
@SerialName(StructWidgetType.CHANNEL_BAR)
open class ChannelBarWidget : StructWidget(), IKmmKeep {


    open val vm: IWidgetChannelBarVM by lazy { ChannelBarVM(this) }

    @Serializable(ChannelBarWidgetDataWrapperSerializer::class)
    open var data: ChannelBarWidgetData? = null

    var action: ChannelBarWidgetAction = ChannelBarWidgetAction()

    override fun getWidgetType() = StructWidgetType.CHANNEL_BAR

    fun getDefaultTab(): String? = data?.default_tab

    fun canShowChannelBar(): Boolean = hasMultiTabs() || action.forceShowChannelBar

    fun hasMultiTabs(): Boolean = data?.channel_list.safeSize() > 1

    // ios可能还在用
    fun widgetChannelBarVM(): IWidgetChannelBarVM = vm

    companion object {
        fun create(items: List<ChannelBarItem>?, defaultTab: String = ""): ChannelBarWidget {
            return ChannelBarWidget().apply {
                data = ChannelBarWidgetData().apply {
                    channel_list = items
                    default_tab = defaultTab
                }
            }
        }

        fun createByChannels(
            channels: List<ChannelWidget>,
            defaultTab: String = ""
        ): ChannelBarWidget {
            val channelBarItems = channels.map { tab ->
                ChannelBarItem(
                    channel_id = tab.channelKey().getNonNull(),
                    channel_name = tab.channelName().getNonNull()
                )
            }
            return create(channelBarItems, defaultTab)
        }
    }

}

@Serializable
class ChannelBarWidgetData : StructWidgetData(), IKmmKeep {
    var default_tab: String = ""
    var channel_list: List<ChannelBarItem>? = null
}

class ChannelBarWidgetDataWrapperSerializer : DataWrapperSerializer<ChannelBarWidgetData>(
    StructWidgetType.CHANNEL_BAR, ChannelBarWidgetData.serializer()
)

@Serializable
class ChannelBarItem(
    var channel_id: String = "",
    var channel_name: String = "",
    var sub_title: String? = null,
    var title_color_light: String? = null,
    var title_color_light_active: String? = null,
    var title_color_night: String? = null,
    var title_color_night_active: String? = null,
    var desc_color_light: String? = null,
    var desc_color_light_active: String? = null,
    var desc_color_night: String? = null,
    var desc_color_night_active: String? = null,
    var icon: String? = null,
    var icon_active: String? = null,
    var subBarItems: List<ChannelBarItem>? = null
) : IKmmKeep

@Serializable
class SelectIcon : IKmmKeep {
    var select_icon: StructImageUrl? = null
    var unselect_icon: StructImageUrl? = null
    var select_text: String = ""
    var unselect_text: String = ""
}

@Serializable
class ChannelBarWidgetAction() : StructWidgetAction(), IKmmKeep {
    var forceShowChannelBar: Boolean = false
}

open class ChannelBarVM(private val widget: ChannelBarWidget) : IWidgetChannelBarVM {

    override val items: List<ChannelBarItem>
        get() = safeList(widget.data?.channel_list)

    override val selectedIndex by lazy { MutableStateFlow(0) }

    override fun changeSelection(index: Int) {
        selectedIndex.update { index }
    }

}