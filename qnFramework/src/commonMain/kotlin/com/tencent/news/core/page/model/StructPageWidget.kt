@file:Suppress(
    "PropertyName",
    "unused",
    "RedundantConstructorKeyword",
    "VariableNaming",
    "ConstructorParameterNaming"
)

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.extension.safeAddAll
import com.tencent.news.core.extension.safeList
import com.tencent.news.core.extension.safeSize
import com.tencent.news.core.list.api.IContextDtoBinding
import com.tencent.news.core.list.api.IContextDtoHolder
import com.tencent.news.core.list.api.IFeedsItemOperator
import com.tencent.news.core.list.api.IListPageIndexInfo
import com.tencent.news.core.list.api.IListRefreshData
import com.tencent.news.core.list.api.ILocalFixTopList
import com.tencent.news.core.list.api.ItemCursor
import com.tencent.news.core.list.model.ChannelShowType
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.JsonToObjSerializer
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.list.trace.getLogStr
import com.tencent.news.core.page.api.IGreyModeConfig
import com.tencent.news.core.page.extension.StructPageWidgetEx.hasDirectoryChannel
import com.tencent.news.core.page.extension.findStructPageConfig
import com.tencent.news.core.page.model.ChannelWidgetAction.Companion.createInitRequestAction
import com.tencent.news.core.page.model.StructWidgetEx.buildSingleWidget
import com.tencent.news.core.page.model.StructWidgetEx.findSingleWidget
import com.tencent.news.core.page.model.StructWidgetParser.parseStructPageWidgetJson
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.tads.api.IAdDataProvider
import com.tencent.news.core.tads.api.IAdHolder
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.qnchannel.api.IChannelInfo
import com.tencent.news.qnchannel.api.IChannelTabProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.min


/**
 * 构建页面widget树，建议使用一系列 buildPageWithXXX 方法
 *
 * 【结构化协议原则】：
 * 1. 【页面结构】不许接入层碰layout，客户端开发在七彩石自己配，后台也不用关注客户端样式字段
 * 2. 【新增组件】需要接入层下发新组件数据时，在七彩石约定新widget_data_type来填充数据，其余的widget信息客户端自己配，不需要接入层写死（新增组件时，接入层需要更新pb文件）
 *
 * @see StructPageWidget2
 */
@Serializable
@SerialName(StructWidgetType.STRUCT_PAGE)
open class StructPageWidget : StructWidget(),
    IWidgetParent<StructPageLayout>,
    ILocalFixTopList,       // 支持'上次看到这里'
    IAdDataProvider,        // 支持广告
    IChannelTabProvider,    // 支持多tab
    IListRefreshData,       // 提供列表数据
    IFeedsItemOperator,     // 列表数据增删改查
    IContextDtoBinding,     // 绑定场景信息
    IGreyModeConfig,        // 黑白模式
    IKmmKeep {

    var titleBar: CommonTitleBarWidget? = null  // 顶部导航条TitleBar

    var header: HeaderWidget? = null            // 头部Header区域（‘品字形’上面的‘口’）
        set(value) {
            field = value
            _headerFlow.update { value }
        }
    private val _headerFlow by lazy { MutableStateFlow<HeaderWidget?>(null) }
    val headerFlow: StateFlow<HeaderWidget?> get() = _headerFlow

    var hanging: StructWidget? = null           // 悬停区域（Header折叠后，会在TitleBar底部悬停）
    var pager: PagerWidget? = null              // 内容区，支持多tab结构（‘品字形’下面的多个‘口’）
    var bottomBar: BottomBarWidget? = null      // 底部导航条BottomBar
    var layers: LayersWidget? = null            // 全屏挂件浮层

    var bg: StructWidget? = null                // 背景图组件（整个页面背后贴一个图或lottie）

    var catalogue: CatalogueWidget?             // 专题目录导航 todo genesisli opt 后面从基类移走
        get() = hanging as? CatalogueWidget
        set(value) {
            hanging = value
        }

    @Serializable(StructPageWidgetDataWrapperSerializer::class)
    var data: StructPageWidgetData? = null

    var action: StructPageWidgetAction = StructPageWidgetAction()

    @Transient
    var originNetData: Any? = null // 存放本地转换成 StructPageWidget 前的原始数据

    @Transient
    var retCode = 0

    @Transient
    var parseError: Throwable? = null

    @Transient
    @kotlin.jvm.Transient
    private val widgetHolder = StructWidgetHolder(this)

    // 置顶列表
    override var localFixTopList: List<IKmmFeedsItem>? = null

    @Transient
    @kotlin.jvm.Transient
    var listPageIndexInfo: IListPageIndexInfo? = null

    override fun getListPageInfo(): IListPageIndexInfo? {
        return listPageIndexInfo
    }

    init {
        bindWidgetProvider(widgetHolder)
    }

    override fun getWidgetType() = StructWidgetType.STRUCT_PAGE

    fun hasMultiChannels(): Boolean = pager?.channels.safeSize() > 1

    override fun wp(): IWidgetProvider = widgetHolder

    fun reBindRootWidget(rootWidget: StructPageWidget) {
        widgetHolder.rootWidget = rootWidget
    }

    fun reBindWidgetEnv(widgetEnv: IWidgetEnv) {
        widgetHolder.widgetEnv = widgetEnv
    }

    /**
     * 根据结构化协议下发的layout构建整个widget树，用于后台是结构化协议的场景（例如：专题页）
     * 注意：json格式应该是[StructPageData]结构的
     */
    fun buildPageWithJson(json: String?) {
        parseStructPageWidgetJson(json)
    }
    
    /**
     * 根据结构化协议下发的layout构建整个widget树，用于后台是结构化协议的场景（例如：专题页）
     * 如果json完全由kmm解析的话，可以直接调用[buildPageWithJson]
     * （widget树构建后，会自动给每个widget绑定[IWidgetProvider]）
     */
    internal fun buildPageWithLayout(
        widgetList: List<StructWidget>?,
        widgetGroup: Map<String, StructWidgetList>?,
        layout: StructPageLayout?,
    ) {
        // 如果后台下发了StructPageWidget数据，拷贝到本地实例里
        val pageWidget = widgetList?.filterIsInstance<StructPageWidget>()?.firstOrNull()
        if (pageWidget != null) {
            this.mergeWidgetData(pageWidget)
        }

        widgetHolder.widgetList = widgetList
        widgetHolder.widgetGroup = widgetGroup

        if (layout != null) {
            buildLayoutWidgets(layout)
        } else { // 后台某些结构可能不下发layout（例如：专题分区的展开）
            buildPageWithContent(ChannelWidget.createDefenseMainChannelWidget(), widgetList)
        }

        compatWidgetBuild()
    }

    /**
     * 适用于端上手动构建widget树，普遍是一些非结构化接口的场景（例如：付费专栏页）
     */
    fun buildPageWithManual(
        enableManualWidgetListCompat: Boolean = false,
        action: StructPageWidget.() -> Unit,
    ): StructPageWidget {
        this.action()

        bindWidgetProviderForAllWidgets()
        if (enableManualWidgetListCompat) {
            widgetHolder.widgetList = getAllWidgets()
        }
        compatWidgetBuild()

        return this
    }

    /**
     * 构建只有content组件，最简单的widget树结构（例如：频道）
     */
    fun buildPageWithContent(channel: ChannelWidget, content: List<StructWidget?>?) {
        buildPageWithManual {
            pager = PagerWidget().apply {
                mainChannel = channel.apply {
                    this.content = safeList(content).toMutableList()
                }
            }
        }
    }

    fun buildPageWithListWidget(
        listWidget: NewsListWidget,
        channel: ChannelWidget = ChannelWidget(),   // 一般构建分页数据时，ChannelWidget用不到可以不传
    ) {
        buildPageWithContent(channel, listOf(listWidget))
    }

    fun buildPageWithItemList(
        newsList: List<IKmmFeedsItem>,
        channel: ChannelWidget = ChannelWidget(),   // 一般构建分页数据时，ChannelWidget用不到可以不传
    ) {
        buildPageWithContent(channel, listOf(NewsListWidget.create(newsList)))
    }

    override fun buildLayoutWidgets(layout: StructPageLayout?) {
        titleBar = buildSingleWidget(layout?.title_bar)
        titleBar?.buildLayoutWidgets(layout?.title_bar)

        header = buildSingleWidget(layout?.header)
        header?.buildLayoutWidgets(layout?.header)

        pager = buildSingleWidget(layout?.pager)
            ?: buildSingleWidget(PagerWidget()) // pager 不能为空，如果没下发创建个默认的
        pager?.buildLayoutWidgets(layout?.pager)

        bottomBar = buildSingleWidget(layout?.bottom)
        bottomBar?.buildLayoutWidgets(layout?.bottom)
    }

    // 如果后台下发不符合客户端协议设计，可以在这一层进行二次加工；保障业务侧组件协议整洁
    open fun compatWidgetBuild() {

        // 放置layout下发有错误，用widgetList作为兜底
        val mainContentWidgets = getMainContentListWidgets()
        if (mainContentWidgets.isEmpty()) {
            pager?.mainChannel?.content = widgetHolder
                .widgetList?.filter { it is IFeedsItemWidget }?.toMutableList()
        }

        if (hanging == null) { // 如果没有手动指定悬停组件，再构造默认的
            // 构建导航目录组件（本地伪造的widget）
            val catalogueWidget = buildSingleWidget(CatalogueWidget())
            catalogueWidget.buildCatalogueData(mainContentWidgets, getValidPageTheme())

            catalogueWidget.action.showAtRight = hasDirectoryChannel() &&
                    catalogueWidget.data?.catalogueData.safeSize() >= 5

            hanging = if (data?.business_type == StructPageBusinessType.TOPIC) {
                catalogueWidget // 话题专题不需要校验数据合法，占个坑就行
            } else if (data?.business_type == StructPageBusinessType.QA) {
                null // 问答专题强制不生成目录组件
            } else {
                catalogueWidget.takeIf { it.isDataValid() }
            }
        }

        // ‘网友热议’按钮，后台没下发commentId，主动绑定下
        val hotSpotBtn = findSingleWidget<HotSpotBtnWidget>()
        hotSpotBtn?.data?.comment_id = getPublishCommentId()
        bindRelateVideo()

        // show_type=143和201的channelWidget增加兜底action，避免第一个tab非默认tab时，第一个tab数据空的问题
        getChannelWidgets().filter {
            it.data?.channel_info?.channelShowType == ChannelShowType.COMMON_LIST ||
                    it.data?.channel_info?.channelShowType == ChannelShowType.HOT_EVENT_WITH_DIRECTORY
        }
            .forEach { channelWidget ->
                data?.event_id?.let { eventId ->
                    if (channelWidget.action.reset == null) {
                        channelWidget.action.reset =
                            channelWidget.data?.channel_info?.createInitRequestAction(eventId)
                    }
                }
            }
    }

    private fun bindRelateVideo() {
        val relateVideoMap = mutableMapOf<String, List<String>>()
        val videoList = data?.video_list
        videoList?.forEachIndexed { index, videoID ->
            relateVideoMap[videoID] = videoList.subList(index + 1, min(index + 5, videoList.size))
        }
        getHeaderFeedsItemListArray().forEach { item ->
            if (relateVideoMap.containsKey(item.baseDto.idStr)) {
                item.ctxDto.relateVideos = relateVideoMap[item.baseDto.idStr]?.toMutableList()
            }
        }
        getFeedsItemListArray().forEach { item ->
            if (relateVideoMap.containsKey(item.baseDto.idStr)) {
                item.ctxDto.relateVideos = relateVideoMap[item.baseDto.idStr]?.toMutableList()
            }
            item.moduleDto.newsModule?.newsList?.forEach { subitem ->
                if (relateVideoMap.containsKey(subitem.baseDto.idStr)) {
                    subitem.ctxDto.relateVideos =
                        relateVideoMap[subitem.baseDto.idStr]?.toMutableList()
                }
            }
        }
    }

    // 优先取 pageStruct 的，空的再用发布按钮上带下来的
    fun getPublishCommentId(): String =
        (data?.comment_id ?: "").ifEmpty {
            findSingleWidget<InputBtnWidget>()?.data?.comment_id ?: ""
        }

    /**
     * 为整个widget树绑定[IWidgetProvider]，一般用于手动创建的[StructPageWidget]场景，没经过layout构建
     */
    protected fun bindWidgetProviderForAllWidgets() {
        this.buildAllWidgets().forEach {
            it.bindWidgetProvider(widgetHolder) // 递归循环绑定 wp
        }
    }

    fun getAllWidgets(): List<StructWidget> = buildAllWidgets()

    internal fun findWidgetInPageWidgetTree(condition: WidgetCondition): List<StructWidget> =
        buildAllWidgets(condition)

    private fun StructWidget.buildAllWidgets(condition: WidgetCondition? = null): List<StructWidget> {
        val result = mutableListOf<StructWidget>()

        if (condition.match(this)) {
            result.add(this)
        }

        if (this is IWidgetParent<*>) {
            this.getSubWidgets()?.forEach { subWidget ->
                val subWidgetList = subWidget.buildAllWidgets()
                result.safeAddAll(subWidgetList.filter { condition.match(it) })
            }
        }

        return result
    }

    private fun WidgetCondition?.match(widget: StructWidget): Boolean =
        this == null || this.invoke(widget)

    override fun getSubWidgets(): List<StructWidget>? =
        safeList(titleBar, header, pager, bottomBar, hanging, layers, bg)

    fun mergeWidgetData(other: StructPageWidget) {
        if (other.data != null) {
            this.data = other.data
        }
    }

    /**
     * 插入到列表中的广告数据
     */
    override fun getAdListJson(): String = getAdListWidget()?.data?.adListJson.getNonNull()

    override fun getAdHolder(): IAdHolder? = getAdListWidget()?.adHolder

    override fun setAdHolder(adHolder: IAdHolder?) {
        getAdListWidget()?.adHolder = adHolder
    }

    fun getAdListWidget(): AdListWidget? = findSingleWidget()

    /**
     * 底层页的子tab
     */
    override fun getTabList(): List<IChannelInfo> =
        getChannelWidgets().mapNotNull { it.data?.channel_info }

    /**
     * 底层页的默认tab
     */
    override fun getDefaultTab(): String? = pager?.channelBar?.getDefaultTab()

    override fun enableGreyMode(): Boolean = data?.grey_mode == 1

    fun isCloseAllAd(): Boolean = data?.close_all_ad == 1

    fun canShowTitleArea(): Boolean = data?.title_switch == 0

    fun getValidPageTheme(): StructPageTheme? {
        val theme = data?.theme
            ?: return null

        // 本地设置标识，禁用皮肤
        if (action.disableSkin || findStructPageConfig()?.forbidSkin.isTrue()) {
            return null
        }

        // shiply统一开关，禁用皮肤
        if (getShiplySwitch("remote_disable_event_detail_skin")) {
            return null
        }

        // 皮肤色值非法
        if (!theme.isDataValid()) {
            return null
        }
        return theme
    }

    /**
     * 获取品字形结构中header区域的内容列表
     */
    fun getHeaderListWidgets(): List<StructWidget> = safeList(header?.headerList)

    fun getHeaderFeedsItemListArray(): Array<IKmmFeedsItem> =
        getHeaderListWidgets().toFeedsItemList().toTypedArray()

    /**
     * 获取品字形结构中 底部bar中的按钮组件列表
     */
    fun getBottomBarBtnWidgets(): List<StructWidget> = safeList(bottomBar?.btnList)

    /**
     * 获取品字形结构中 所有的子频道widget
     */
    fun getChannelWidgets(): List<ChannelWidget> = safeList(pager?.channels)

    fun findChannelWidget(channelKey: String): ChannelWidget? =
        getChannelWidgets().find { it.data?.channel_info?.channelKey == channelKey }

    /**
     * 获取品字形结构中 主频道首屏内容的widget列表
     */
    fun getMainContentWidgets(): List<StructWidget> = safeList(pager?.mainChannel?.content)

    fun isMainContentEmpty(): Boolean = getMainContentWidgets().isEmpty()

    fun getFeedsItemListArray(): Array<IKmmFeedsItem> =
        getMainContentFeedsItemWidgets().toFeedsItemList().toTypedArray()

    open fun getMainContentListWidgets(): List<NewsListWidget> =
        getMainContentWidgets().filterIsInstance<NewsListWidget>()

    fun getMainContentFeedsItemWidgets(): List<StructWidget> =
        getMainContentWidgets().filter { it is IFeedsItemWidget }

    fun appendMainContentWidgets(newPageWidget: StructPageWidget?) {
        val widgetList = newPageWidget?.getMainContentFeedsItemWidgets()
            ?: return
        this.pager?.mainChannel?.content?.addAll(widgetList)
    }

    fun replaceMainContentWidgets(newPageWidget: StructPageWidget?): List<IKmmFeedsItem>? {
        val widgetList = newPageWidget?.getMainContentFeedsItemWidgets()
            ?: return null
        this.pager?.mainChannel?.content = widgetList.toMutableList()

        return newPageWidget.getFeedsList()
    }

    fun removeMainContentWidgets(condition: (StructWidget) -> Boolean) {
        pager?.mainChannel?.content?.removeAll(condition)
    }

    fun replaceAllWidgets(newPageWidget: StructPageWidget?): List<IKmmFeedsItem>? {
        newPageWidget ?: return null

        this.titleBar = newPageWidget.titleBar
        this.header = newPageWidget.header
        this.pager = newPageWidget.pager
        this.bottomBar = newPageWidget.bottomBar
        this.hanging = newPageWidget.hanging
        this.layers = newPageWidget.layers
        this.bg = newPageWidget.bg

        this.mergeWidgetData(newPageWidget)

        this.action = newPageWidget.action
        this.originNetData = newPageWidget.originNetData

        if (this.widgetHolder.widgetEnv == null) {
            this.widgetHolder.widgetEnv = newPageWidget.getWidgetEnv()
        }

        // 为了应对有的dataRepo构建pageWidget没使用buildPageWithManual，这里再手动绑定一下作为兜底；
        // 否则 子widget.findStructPageWidget() 找不到东西
        bindWidgetProviderForAllWidgets()

        return newPageWidget.getFeedsList()
    }

    open fun pageSkinRes(): PageSkinRes? {
        val dayTheme = getValidPageTheme()
            ?: return null

        val nightTheme = data?.night_theme?.takeIf { it.isDataValid() }
            ?: dayTheme.mapToNightTheme()

        return PageSkinRes(dayTheme, nightTheme)
    }

    /**
     * 用于展示的列表文章
     */
    override fun getFeedsList(): List<IKmmFeedsItem> =
        getMainContentFeedsItemWidgets().toFeedsItemList()

    override fun insertFeedsItem(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Boolean {
        getMainContentListWidgets().forEach { widget ->
            val insertIndex = widget.insertItemListWithCursor(newData, cursor)
            if (insertIndex != INVALID_NUM) {
                NewsChannelLog.fileLog("Widget", "【列表插入】${newData}")
                return true
            }
        }
        return false
    }

    override fun insertFeedsItemAfter(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Boolean {
        getMainContentListWidgets().forEach { widget ->
            val insertIndex = widget.insertItemListAfterCursor(newData, cursor)
            if (insertIndex != INVALID_NUM) {
                NewsChannelLog.fileLog("Widget", "【列表后插】${newData}")
                return true
            }
        }
        return false
    }

    override fun appendFeedsItem(newData: List<IKmmFeedsItem>): Boolean {
        val lastWidget = getMainContentListWidgets().lastOrNull()
        if (lastWidget != null) {
            val result = lastWidget.appendItemList(newData)
            if (result) {
                NewsChannelLog.fileLog("Widget", "【列表追加】size=${newData.size}")
            }
            return result
        }
        return false
    }

    override fun removeFeedsItem(cursor: ItemCursor): List<IKmmFeedsItem> {
        val removedList = mutableListOf<IKmmFeedsItem>()
        getMainContentListWidgets().forEach { widget ->
            val removedItems = widget.removeItems(cursor)
            removedItems?.forEach { removed ->
                removedList.add(removed)
                NewsChannelLog.fileLog("Widget", "【列表删除】${removed.getLogStr()}")
            }
        }
        return removedList
    }

    override fun replaceFeedsItem(
        newData: List<IKmmFeedsItem>,
        cursor: ItemCursor,
    ): IKmmFeedsItem? {
        getMainContentListWidgets().forEach { widget ->
            val replacedItem = widget.replaceItem(newData, cursor)
            if (replacedItem != null) {
                NewsChannelLog.fileLog("Widget", "【列表替换】${replacedItem}->${newData}")
                return replacedItem
            }
        }
        return null
    }

    override fun findFeedsItem(cursor: ItemCursor): IKmmFeedsItem? {
        getMainContentListWidgets().forEach { widget ->
            val result = widget.findItem(cursor)
            if (result != null) {
                return result
            }
        }
        return null
    }

    override fun getAllFeedsItemList(): List<IKmmFeedsItem> = getFeedsList()

    /**
     * 预加载的，用于tab2首屏的文章
     */
    override fun getExtraList(): List<IKmmFeedsItem>? =
        getMainContentListWidgets().firstOrNull()?.data?.extra_list

    /**
     * 分页透传字段
     */
    override fun getListTransParam(): String? = getMainListData()?.listTransParam

    /**
     * 刷新文案
     */
    override fun getRefreshWording(): String? = getMainListData()?.refreshWording

    override fun disableNewsReplace(): String? {
        return getMainListData()?.disable_news_replace
    }

    /**
     * 列表加载完成的底部文案
     */
    override fun getLoadedFinishText(): String? = getMainListData()?.loadFinishText

    // 底部加载更多
    override fun hasMore(): Boolean =
        getMainContentListWidgets().firstOrNull { it.canAutoLoadMore() } != null

    // 顶部加载更多（例如：付费专栏页 会有）
    fun hasTopMore(): Boolean =
        getMainContentListWidgets().firstOrNull { it.canAutoTopMore() } != null

    fun isFeedsDataValid(): Boolean {
        if (hasMore() && getFeedsList().isEmpty()) {
            // 后台下发‘还有更多数据’，但列表数据为空，认为是出错
            return false
        }

        // 暂无其他校验，默认认为合法
        return true
    }

    /**
     * 刷新时间戳
     */
    override fun getRefreshTimestamp(): Long = getMainListData()?.timestamp ?: 0

    private fun getMainListData(): ListRefreshInfo? =
        getMainContentListWidgets().firstOrNull()?.data?.extra

    override fun getContextDtoBindingTargets(): List<IContextDtoHolder>? {
        val result = arrayListOf<IKmmFeedsItem>()
        result.safeAddAll(getFeedsList())

        // 本地展开的Item，也要绑定数据
        getMainContentListWidgets().forEach { listWidget ->
            listWidget.action?.load_more?.request?.forEach { request ->
                result.safeAddAll(request.local_data?.widget_list?.toFeedsItemList())
            }
        }

        return result
    }

    override fun toString(): String {
        return "【${getWidgetType()}|${widget_id}】ret=${getResultCode()}" +
                ", hasMore=${hasMore()}" +
                ", mainWidgetSize=${getMainContentWidgets().size}" +
                ", itemSize=${getFeedsList().size}" +
                ", closeAd=${isCloseAllAd()}" +
                ", transparam=${getListTransParam()}" +
                ", titleBar=${titleBar}" +
                ", header=${header}" +
                ", bottomBar=${bottomBar}"
    }

}

@Suppress("VariableNaming")
@Serializable
class StructPageWidgetData : StructWidgetData(), IKmmKeep {
    var grey_mode: Int = 0                  // 黑白模式开关

    var theme: StructPageTheme? = null          // 皮肤配置（日间）
    var night_theme: StructPageTheme? = null    // 皮肤配置（夜间）7450预留，客户的自己生成，后续做成后台可干预下发

    var close_all_ad: Int = 0               // 广告开关

    @StructPageBusinessType
    var business_type: String = ""          // 业务类型 定义在上层业务 StructPageBusiness

    var video_list: List<String>? = null    // 专题所有视频id List

    var title_switch: Int = 0               // 标题区域是否显示，默认显示0，隐藏1

    var isColumnPay: Boolean = false        // 【付费专栏】是否已购买

    var comment_id: String = ""             // 专题的 commentId
    var event_id: String = ""               // 专题的 cmsId

    var comment_skin_switch: Int = 0        // 专题评论是否展示皮肤开关

    var close_listen_icon: Int = 0          // 是否展示音频按钮 0-显示 1-隐藏
}

class StructPageWidgetDataWrapperSerializer : DataWrapperSerializer<StructPageWidgetData>(
    StructWidgetType.STRUCT_PAGE, StructPageWidgetData.serializer()
)

internal object StructPageLayoutSerializer : JsonToObjSerializer<StructPageLayout>({
    StructPageLayout.serializer()
})

@Serializable
data class StructPageLayout(
    var layout_id: String = "",
    var layout_type: String = "",
    var datasource_type: String = "",

    var title_bar: TitleBarWidgetLayout? = null,
    var header: HeaderWidgetLayout? = null,
    var pager: PagerWidgetLayout? = null,
    var bottom: BottomBarWidgetLayout? = null,
) : StructWidgetLayout(), IKmmKeep {

    fun isEmpty(): Boolean {
        return title_bar == null &&
                header == null &&
                pager == null &&
                bottom == null
    }
}

@Serializable
class StructPageWidgetAction : StructWidgetAction(), IKmmKeep {
    var disableSkin = false             // 强制禁用皮肤（例如嵌入到热点精选里）

    var disableTopRefresh = false       // 强制禁用顶部下拉刷新（预留，暂未实现）
    var disableBottomRefresh = false    // 强制禁用底部上拉加载（预留，暂未实现）
}

@Serializable
class CatalogueSection : IKmmKeep {
    var key: String = ""                // 跳转用的key（section的name字段）
    var catalogueName: String = ""      // 目录外显的名称（可能与列表分区标题不同）

    @Transient
    var theme: StructPageTheme? = null  // 页面皮肤配置
}
