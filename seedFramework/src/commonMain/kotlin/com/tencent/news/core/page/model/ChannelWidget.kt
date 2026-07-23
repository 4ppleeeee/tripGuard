package com.tencent.news.core.page.model

import com.tencent.news.core.channel.model.QnKmmChannelInfo
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.model.ChannelShowType
import com.tencent.news.core.list.model.NormalListConfig
import com.tencent.news.core.list.model.QnListConfig
import com.tencent.news.core.list.model.RequestCgi
import com.tencent.news.core.list.model.new
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.extension.findStructPageConfig
import com.tencent.news.core.page.model.StructWidgetEx.buildWidgetList
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.user.model.QnUserInfo
import com.tencent.news.qnchannel.api.IChannelInfo
import com.tencent.news.qnchannel.api.getRequestCgi
import com.tencent.news.qnchannel.api.getWidgetAction
import com.tencent.news.qnchannel.api.logKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable

@SerialName(StructWidgetType.CHANNEL)
open class ChannelWidget(
    @Serializable(ChannelWidgetDataWrapperSerializer::class)
    open var data: ChannelWidgetData? = null,

    var content: MutableList<StructWidget>? = null,

    var action: ChannelWidgetAction = ChannelWidgetAction(),

    @Transient
    open var dtReport: PageDtReport? = null,
) : StructWidget(), IWidgetParent<ChannelLayout> {

    val status by lazy { ChannelWidgetStatus() }

    var contentBg: StructWidget? = null // 内容列表背景

    var empty: StructWidget? = null // 自定义空页面样式

    // 是否强制使用夜间模式（用于错误页面）
    @Transient
    var forceDarkTheme: Boolean = false

    // 页面背景色（用于设置子tab页面的背景色）
    @Transient
    var backgroundColor: Long? = null

    // 页面渐变背景（用于设置子tab页面的渐变背景色）
    @Transient
    var backgroundGradientStops: List<Pair<Float, Long>>? = null

    // ChannelWidget 作为子tab时，缓存已拉取的数据
    // （目前compose里支持，见：StructChannelList）
    @Transient
    var subTabFeedsCtrl: IFlexibleFeedsController? = null

    // ChannelWidget 作为子tab时，缓存对应的ViewModel
    // 用于外部触发刷新（例如：广场item插入后触发电台tab刷新）
    @Transient
    var subTabPageViewModel: Any? = null
        set(value) {
            field = value
            if (value != null) {
                notifySubTabPageViewModelReady(value)
            }
        }

    @Transient
    private val subTabPageViewModelObserverSet = LinkedHashSet<ISubTabPageViewModelObserver>()

    fun addSubTabPageViewModelObserver(observer: ISubTabPageViewModelObserver) {
        subTabPageViewModelObserverSet.add(observer)
        subTabPageViewModel?.let { observer.onSubTabPageViewModelReady(it) }
    }

    fun removeSubTabPageViewModelObserver(observer: ISubTabPageViewModelObserver) {
        subTabPageViewModelObserverSet.remove(observer)
    }

    private fun notifySubTabPageViewModelReady(viewModel: Any) {
        subTabPageViewModelObserverSet.toList().forEach {
            it.onSubTabPageViewModelReady(viewModel)
        }
    }

    override fun getWidgetType() = StructWidgetType.CHANNEL

    override fun buildLayoutWidgets(layout: ChannelLayout?) {
        layout ?: return
        content = buildWidgetList(layout.widget_list)
    }

    override fun getSubWidgets(): List<StructWidget>? {
        return safeList(listOf(contentBg), content)
    }

    fun matchTabId(tabId: String?): Boolean {
        tabId ?: return false
        return tabId == data?.channel_info?.channelKey
    }

    companion object {
        fun createDefenseMainChannelWidget(): ChannelWidget = create("all", "综合")

        fun create(
            channelId: String,
            channelName: String,
            showType: Int = ChannelShowType.COMMON_LIST
        ) = ChannelWidget(
            data = ChannelWidgetData.create(channelId, channelName, showType)
        )

        fun ChannelWidget?.enableFooter(): Boolean {
            this ?: return false
            val enableFooterForPage = findStructPageConfig()?.enableFooter ?: true
            val enableFooterForChannel = status.enableFooter
            return enableFooterForPage && enableFooterForChannel
        }

        fun ChannelWidget?.enableHeader(): Boolean {
            this ?: return false
            val config = findStructPageConfig()
            // 当找不到外层 StructPageConfig 时（如子 tab 场景），直接使用 status.enableHeader 兜底
            val enableHeaderForPage = config?.enableHeader ?: return status.enableHeader
            val enableHeaderForChannel = status.enableHeader
            return enableHeaderForPage && enableHeaderForChannel
        }

    }

}

interface ISubTabPageViewModelObserver {
    fun onSubTabPageViewModelReady(viewModel: Any)
}

class ChannelWidgetDataWrapperSerializer : DataWrapperSerializer<ChannelWidgetData>(
    StructWidgetType.CHANNEL, ChannelWidgetData.serializer()
)

fun ChannelWidget?.pickInitRequest(): DataRequest? {
    return this?.action?.reset?.request.pickOne()
}

fun ChannelWidget?.pickRefreshRequest(): DataRequest? {
    return this?.action?.refresh?.request.pickOne()
}

fun ChannelWidget?.removeRequest(dataRequest: DataRequest?) {
    this?.action?.apply {
        refresh?.request?.remove(dataRequest)
    }
}

fun ChannelWidget?.channelKey() = this?.data?.channel_info?.channelKey
fun ChannelWidget?.channelName() = this?.data?.channel_info?.channelName

/**
 * 结构化频道中，[ChannelWidget]的数据由2部分组成：主要数据来自大圣，额外信息随接口下发；
 * 此处会将2部分数据进行合并
 */
fun ChannelWidget?.updateChannelWidget(newData: ChannelWidget) {
    this ?: return

    val filterAction = newData.action?.filter
    if (filterAction != null) {
        this.action?.filter = filterAction
    }
}

// pager默认tab选中位置（找不到会默认0）
fun List<ChannelWidget>?.findDefaultTabIndex(defaultTab: String?): Int =
    this?.indexOfFirst { it.matchTabId(defaultTab) }?.takeIf { it > 0 } ?: 0

@Serializable
open class ChannelWidgetData(
    var channel_info: QnKmmChannelInfo? = null,
    var user_info: QnUserInfo? = null,
) : StructWidgetData() {

    companion object {
        fun create(
            channelKey: String,
            channelName: String,
            showType: Int = ChannelShowType.COMMON_LIST,
            subTitle: String? = null
        ) = ChannelWidgetData(
            channel_info = IChannelInfo.new {
                this.channelKey = channelKey
                this.channelName = channelName
                this.channelShowType = showType
                this.sub_title = subTitle
            }
        )
    }
}

@Serializable
data class ChannelLayout(
    var widget_list: MutableList<StructWidgetRef>? = null
) : StructWidgetLayout()

class ChannelWidgetStatus() {
    var enableFooter: Boolean = true        // 是否显示底部加载更多
    var enableHeader: Boolean = false       // 是否显示顶部下拉刷新
    var normalListConfig: QnListConfig = NormalListConfig()
    var bigWindowListConfig: QnListConfig = NormalListConfig()
    var superBigWindowListConfig: QnListConfig = NormalListConfig()
    var listDecorationState: ChannelListDecorationState = ChannelListDecorationState()

    var initIndex: Int = 0                  // 默认选中位置

    var enableScrollPositionRestore: Boolean = false    // 是否启用滚动位置恢复（主要用于二级tab切换时记住浏览位置）
    var userScrolledIndex: Int? = null
    var userScrolledOffset: Int? = null
    var firstVisibleItemIndex: Int? = null  // 当前第一个可见 item 的 index（实时更新）
    var lastVisibleItemIndex: Int? = null  // 当前最后一个可见 item 的 index（实时更新）

    val isSelected = MutableStateFlow(false)
}

data class ChannelListDecorationState(
    val listBackgroundStyle: ChannelListBackgroundStyle = ChannelListBackgroundStyle.NONE,
    val sectionBackgroundStyle: ChannelListBackgroundStyle = ChannelListBackgroundStyle.NONE,
    val sectionBackgroundStartAfterItemId: String = ""
)

enum class ChannelListBackgroundStyle {
    NONE,
    BG_BLOCK,
    BG_PAGE
}

@Serializable
class ChannelWidgetAction : StructWidgetAction() {
    @SerialName("init") // ios中不允许变量以 init 开头
    var reset: DataRequestAction? = null    // 频道首屏请求
    var refresh: DataRequestAction? = null  // 频道下拉刷新
    var filter: DataFilterAction? = null    // 数据筛选（预留的，暂未使用）

    companion object {

        /**
         * 创建频道默认的[ChannelWidget]实现
         */
        fun IChannelInfo?.createChannelWidget(): ChannelWidget {
            val result = ChannelWidget()

            val info = this

            result.data = ChannelWidgetData().apply {
                channel_info = info
            }

            val widgetAction = parseConfigChannelWidgetAction()
                ?: createDefaultChannelWidgetAction()

            if (widgetAction.reset == null) { // init action不能为空，做个保护
                widgetAction.reset = createRequestAction(ListRefreshForward.RESET)
            }

            result.action = widgetAction

            return result
        }

        fun IChannelInfo?.parseConfigChannelWidgetAction(): ChannelWidgetAction? {
            val actionJson = getWidgetAction()
            if (actionJson.isNullOrBlank()) return null

            kotlin.runCatching { // 防止云端配错，解析失败
                return ChannelWidgetAction.fromJson(actionJson)
            }.getOrElse { error ->
                NewsChannelLog.error(logKey(), "widget_action解析失败", error)
            }
            return null
        }

        fun IChannelInfo?.createDefaultChannelWidgetAction(): ChannelWidgetAction {
            return ChannelWidgetAction().apply {
                reset = createRequestAction(ListRefreshForward.RESET)

                refresh = createRequestAction(ListRefreshForward.TOP_REFRESH)

                filter = null
            }
        }

        fun IChannelInfo?.createRequestAction(forward: ListRefreshForward): DataRequestAction {
            return DataRequestAction().apply {
                trigger_type = RequestTrigger.AUTO

                request = mutableListOf(DataRequest().apply {
                    type = RequestType.REQUEST

                    service = "/" + (getRequestCgi() ?: RequestCgi.CHANNEL_FEED)

                    reqdata = mutableMapOf(
                        "forward" to forward.code.toString()
                    )
                })
            }
        }


        fun IChannelInfo?.createDefaultChannelWidgetInitAction(eventId: String): ChannelWidgetAction {
            return ChannelWidgetAction().apply {
                reset = createInitRequestAction(eventId = eventId)
                filter = null
            }
        }

        fun IChannelInfo?.createInitRequestAction(eventId: String): DataRequestAction {
            return DataRequestAction().apply {
                trigger_type = RequestTrigger.INIT

                request = mutableListOf(DataRequest().apply {
                    type = RequestType.REQUEST

                    service = "/gw/page/event_detail_content"

                    reqdata = mutableMapOf(
                        "dataType" to "hot_event_new",
                        "eventId" to eventId,
                        "tabId" to "all"
                    )
                })
            }
        }

        fun fromJson(json: String): ChannelWidgetAction {
            return KtJson.safeDecode(serializer(), json) ?: ChannelWidgetAction()
        }
    }
}
