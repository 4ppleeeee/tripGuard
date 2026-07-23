@file:Suppress("PropertyName")

package com.tencent.news.core.page.model


import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.page.model.StructWidgetEx.buildSingleWidget
import com.tencent.news.core.page.model.StructWidgetEx.buildWidgetList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName(StructWidgetType.COMMON_PAGER)
open class PagerWidget : StructWidget(), IWidgetParent<PagerWidgetLayout> {

    var channelBar: ChannelBarWidget? = null

    var channels: MutableList<ChannelWidget> = mutableListOf()
        set(value) {
            field = value
            if (mainChannel == null) {
                mainChannel = value.firstOrNull()
            }
        }

    var mainChannel: ChannelWidget? = null
        set(value) {
            if (value != null && !channels.contains(value)) {
                channels.add(0, value)
            }
            field = value
            _mainChannelFlow.update { value }
        }

    private val _mainChannelFlow by lazy { MutableStateFlow<ChannelWidget?>(null) }
    val mainChannelFlow: StateFlow<ChannelWidget?> get() = _mainChannelFlow

    var loading: StructWidget? = null // 自定义loading样式（pager级别，不会挡住header）

    val action: PagerWidgetAction = PagerWidgetAction()

    override val asWidgetVM: IPagerWidgetVM = PagerWidgetVM()

    override fun getWidgetType() = StructWidgetType.COMMON_PAGER

    override fun buildLayoutWidgets(layout: PagerWidgetLayout?) {
        layout ?: return

        channelBar = buildWidgetList<ChannelBarWidget>(safeList(layout.channel_bar)).firstOrNull()

        channels = buildWidgetList<ChannelWidget>(layout.tab_list)

        val defaultTabId = channelBar?.data?.default_tab
        val mainChannel = channels.find { it.matchTabId(defaultTabId) } // 优先按照默认 tab 查找
            ?: channels.firstOrNull()                                   // 如果没下发默认，将首个 tab 当做默认
            ?: createDefaultMainChannelWidget()                         // 如果频道组件都没下发，创建兜底的，主频道组件不能为空
        mainChannel.buildLayoutWidgets(layout.content)
        this.mainChannel = mainChannel
    }

    override fun getSubWidgets(): List<StructWidget>? {
        return safeList(listOf(channelBar, mainChannel), channels)
    }

    private fun createDefaultMainChannelWidget(): ChannelWidget {
        val widget = getWidgetEnv()?.createDefaultChannelWidget()   // 优先由业务侧创建兜底的 ChannelWidget
            ?: ChannelWidget.createDefenseMainChannelWidget()       // 如果业务侧也没注入，做个最终保障（尽量不要依赖这个）

        return buildSingleWidget(widget)
    }

}

interface IPagerWidgetVM : IStructWidgetVM {
    val loadingFlow: StateFlow<Boolean>
    fun setLoading(isLoading: Boolean)
}

private class PagerWidgetVM : IPagerWidgetVM {
    override val loadingFlow: MutableStateFlow<Boolean> by lazy {
        MutableStateFlow(false)
    }

    override fun setLoading(isLoading: Boolean) {
        loadingFlow.update { isLoading }
    }
}

@Serializable
data class PagerWidgetAction(
    var initIndex: Int = 0,                     // 默认选中位置
    var beyondViewportPageCount: Int = 0,       // 预加载子tab个数
) : IKmmKeep

@Serializable
data class PagerWidgetLayout(
    var channel_bar: StructWidgetRef? = null,
    var tab_list: List<StructWidgetRef>? = null,
    var content: ChannelLayout? = null
) : StructWidgetLayout()