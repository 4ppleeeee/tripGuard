@file:Suppress("PropertyName")

package com.tencent.news.core.page.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeAddAll
import com.tencent.news.core.extension.safeRemove
import com.tencent.news.core.extension.safeReplaceList
import com.tencent.news.core.extension.safeSize
import com.tencent.news.core.list.api.ItemCursor
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.model.IOriginJson
import com.tencent.news.core.list.model.JsonToObjSerializer
import com.tencent.news.core.list.model.QnKmmFeedsItemList
import com.tencent.news.core.list.model.SafeMutableListSerializer
import com.tencent.news.core.page.model.NewsListWidgetEx.appendNewDataForTimeSliderComponent
import com.tencent.news.core.page.model.NewsListWidgetEx.isTimeSliderComponent
import com.tencent.news.core.tads.constants.INVALID_NUM
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.min

typealias ListWidget = NewsListWidget // 简化一下命名

// 为了方便迁移主干逻辑留了一层，后续应该合并到 NewsListWidget
@Serializable
@SerialName(StructWidgetType.NEWS_LIST)
open class NewsListWidget() : StructWidget(), IFeedsItemWidget {

    @Serializable(NewsListWidgetDataWrapperSerializer::class)
    var data: NewsListWidgetData? = null

    var action: NewsListWidgetAction? = null

    val nonNullAction: NewsListWidgetAction
        get() {
            val result = this.action ?: NewsListWidgetAction()
            this.action = result
            return result
        }

    override fun getWidgetType() = StructWidgetType.NEWS_LIST

    override fun toString(): String {
        return "【${getWidgetType()}|${widget_id}】${data?.section?.section ?: ""}" +
                ", size:${data?.newslist.safeSize()}" +
                ", lazy:${pickLazyInitRequest() != null}" +
                ", more:${action?.load_more?.request.safeSize()}"
    }

    override fun getItemWidgets(): List<IKmmFeedsItem> {
        return data?.newslist ?: emptyList()
    }

    fun forEach(action: (IListItem) -> Unit) {
        data?.newslist?.forEach(action)
    }

    // ========== request 相关处理 ==========
    fun canLazyInit(): Boolean {
        return pickLazyInitRequest() != null
    }

    fun canAutoLoadMore(): Boolean {
        return pickLazyInitRequest() != null || pickAutoLoadMoreRequest() != null
    }

    fun canAutoTopMore(): Boolean {
        return pickAutoTopMoreRequest() != null
    }

    fun canClickLoadMore(): Boolean {
        return pickClickRequest() != null
    }

    fun pickLazyInitRequest(): DataRequest? {
        return action?.lazy_init?.pickAnyRequest()
    }

    fun pickAutoLoadMoreRequest(): DataRequest? {
        return action?.load_more?.pickAutoRequest()
    }

    fun pickAutoTopMoreRequest(): DataRequest? {
        return action?.top_more?.pickAutoRequest()
    }

    fun pickClickRequest(): DataRequest? {
        return action?.load_more?.pickClickRequest()
    }

    fun removeRequest(request: DataRequest?) {
        request ?: return
        action?.apply {
            lazy_init?.request?.safeRemove(request)
            load_more?.request?.safeRemove(request)
            top_more?.request?.safeRemove(request)
        }
    }

    fun appendNewWidgetData(newWidgetData: StructPageWidget): List<IKmmFeedsItem> {
        val newFeedsItemList = newWidgetData.getMainContentFeedsItemWidgets().toFeedsItemList()

        // 追加文章数据
        if (isTimeSliderComponent()) {
            appendNewDataForTimeSliderComponent(newFeedsItemList)
        } else {
            val newsList = data?.newslist ?: arrayListOf()
            newsList.addAll(newFeedsItemList)
            data?.newslist = newsList
        }

        // 追加翻页request
        val newListWidgets = newWidgetData.getMainContentListWidgets()
        newListWidgets.forEach { listWidget ->
            action?.load_more?.request?.safeAddAll(0, listWidget.action?.load_more?.request)
        }

        return newFeedsItemList
    }

    fun topInsertNewWidgetData(newWidgetData: StructPageWidget): List<IKmmFeedsItem> {
        val newFeedsItemList = newWidgetData.getMainContentFeedsItemWidgets().toFeedsItemList()

        // 记录插入前的列表大小，用于计算滚动位置
        val originalSize = data?.newslist?.size ?: 0

        // 追加文章数据
        val newsList = data?.newslist ?: arrayListOf()
        newsList.addAll(0, newFeedsItemList)
        data?.newslist = newsList

        // 追加翻页request
        val newListWidgets = newWidgetData.getMainContentListWidgets()
        newListWidgets.forEach { listWidget ->
            action?.top_more?.request?.safeAddAll(0, listWidget.action?.top_more?.request)
        }

        // 设置滚动到原来位置（新插入数据的末尾）
        if (newFeedsItemList.isNotEmpty()) {
            data?.lastDisplayTopIndex = newFeedsItemList.size
        }

        return newFeedsItemList
    }

    fun removeItem(cursor: ItemCursor): IKmmFeedsItem? {
        val newsList = data?.newslist
            ?: return null
        val result = newsList.find(cursor)
        newsList.remove(result)
        return result
    }

    fun removeItems(cursor: ItemCursor): List<IKmmFeedsItem>? {
        val newsList = data?.newslist
            ?: return null
        val result = newsList.filter(cursor)
        newsList.removeAll(result)
        return result
    }

    fun findItem(cursor: ItemCursor): IKmmFeedsItem? {
        return data?.newslist?.find(cursor)
    }

    fun indexOfItem(cursor: ItemCursor): Int {
        return data?.newslist?.indexOfFirst(cursor) ?: INVALID_NUM
    }

    fun insertItemList(newData: List<IKmmFeedsItem>, index: Int): Int {
        val newsList = data?.newslist
            ?: return INVALID_NUM

        if (index >= 0) {
            newsList.safeAddAll(index, newData)
            return min(index, getListSize())
        }
        return INVALID_NUM
    }

    fun insertItemListWithCursor(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Int {
        val newsList = data?.newslist
            ?: return INVALID_NUM

        return insertItemList(newData, newsList.indexOfFirst(cursor))
    }

    fun insertItemListAfterCursor(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Int {
        val newsList = data?.newslist
            ?: return INVALID_NUM

        val index = newsList.indexOfFirst(cursor)
        if (index < 0) return INVALID_NUM

        // after语义由QNCore内部落到锚点后一位，调用方不再需要从展示列表反查下一个item。
        return insertItemList(newData, index + 1)
    }

    fun appendItemList(newData: List<IKmmFeedsItem>): Boolean {
        if (data == null) {
            data = NewsListWidgetData()
        }
        if (data?.newslist == null) {
            data?.newslist = mutableListOf()
        }
        data?.newslist?.safeAddAll(newData)
        return true
    }

    fun replaceItem(newData: List<IKmmFeedsItem>, cursor: ItemCursor): IKmmFeedsItem? {
        val newsList = data?.newslist
            ?: return null

        val oldData = findItem(cursor)
            ?: return null

        newsList.safeReplaceList(oldData, newData)
        return oldData
    }

    fun getListSize(): Int {
        return data?.newslist.safeSize()
    }

    fun getSectionName(): String {
        return data?.section?.name ?: ""
    }

    companion object {
        fun create(newsList: List<IKmmFeedsItem>) = NewsListWidget().apply {
            data = NewsListWidgetData().apply {
                newslist = newsList.toMutableList()
            }
        }

        // 快速构建带底刷翻页的listWidget
        fun create(
            newsList: List<IListItem>,
            loadMoreService: String,
            loadMoreParams: ReqDataMap = null
        ) = NewsListWidget().apply {
            data = NewsListWidgetData().apply {
                newslist = newsList.toMutableList()
            }

            if (loadMoreService.isNotEmpty()) {
                action = NewsListWidgetAction().apply {
                    load_more = NewsListWidgetAction.createLoadMoreAction(
                        loadMoreService, loadMoreParams
                    )
                }
            }
        }

    }

}

class NewsListWidgetDataWrapperSerializer : DataWrapperSerializer<NewsListWidgetData>(
    StructWidgetType.NEWS_LIST, NewsListWidgetData.serializer()
)

// 文章列表解析，会过滤掉数组中的null
object SafeItemListSerializer : SafeMutableListSerializer<IKmmFeedsItem>(
    serializer = IKmmFeedsItem.QnSerializer
)

@Serializable
class NewsListWidgetData : StructWidgetData() {
    var newslist: QnKmmFeedsItemList = null     // 文章列表
    var section: NewsListSection? = null        // 模块分区数据

    @Serializable(ListRefreshInfoSerializer::class)
    var extra: ListRefreshInfo? = null          // 列表刷新信息（二级频道会用到，例如 channel_feed 接口）
    var extra_list: QnKmmFeedsItemList = null   // 额外列表（目前主要用于下发tab2预加载的数据）
    var lastDisplayTopIndex: Int? = null         // 上次显示时的顶部索引位置

    fun getNewsListArray(): Array<IKmmFeedsItem> {
        return newslist?.toTypedArray() ?: arrayOf()
    }
}

@Suppress("PrivatePropertyName", "SpellCheckingInspection")
@Serializable // 【注意】这个类需要同时兼容Gson和ktx，不能用 @SerialName
class ListRefreshInfo : IOriginJson, IKmmKeep {

    override var originJson: String = "" // 目前ios还有些老字段兼容要用

    var loadFinishText: String = "" // 加载完成文案，例如 "别拉了，真没了"

    private var recommWording: String = ""
    var refreshWording: String          // 刷新文案，例如 "又发现了#n#条新内容"
        get() = recommWording
        set(value) {
            recommWording = value
        }

    var timestamp: Long = 0             // 刷新时间戳

    var disable_news_replace: String? = "0"  // 禁止云重排
    private var list_transparam: String = ""
    var listTransParam: String          // 刷新透传字段
        get() = list_transparam
        set(value) {
            list_transparam = value
        }

}

object ListRefreshInfoSerializer : JsonToObjSerializer<ListRefreshInfo>(
    { ListRefreshInfo.serializer() }
)

typealias ReqDataMap = Map<String, String>?

@Serializable
class NewsListWidgetAction : StructWidgetAction() {
    var lazy_init: DataRequestAction? = null    // 延迟加载
    var load_more: DataRequestAction? = null    // 底部加载
    var top_more: DataRequestAction? = null     // 顶部加载

    var forbidDistinctFilter: Boolean = false   // 禁掉数据排重

    companion object {
        fun createAutoRefreshAction(
            listRefreshForward: ListRefreshForward,
            service: String,
            reqData: ReqDataMap = null,
        ): DataRequestAction {
            val params = reqData?.toMutableMap() ?: mutableMapOf()
            params["forward"] = listRefreshForward.code.toString()

            return DataRequestAction().apply {
                trigger_type = RequestTrigger.AUTO
                request = arrayListOf(DataRequest().apply {
                    this.type = RequestType.REQUEST
                    this.service = safeGetService(service)
                    this.reqdata = params
                })
            }
        }

        fun createClickRefreshAction(
            listRefreshForward: ListRefreshForward,
            service: String,
            reqData: ReqDataMap = null,
        ): DataRequestAction {
            val params = reqData?.toMutableMap() ?: mutableMapOf()
            params["forward"] = listRefreshForward.code.toString()

            return DataRequestAction().apply {
                trigger_type = RequestTrigger.CLICK
                request = arrayListOf(DataRequest().apply {
                    this.type = RequestType.REQUEST
                    this.service = safeGetService(service)
                    this.reqdata = params
                })
            }
        }

        fun createLoadMoreAction(service: String, reqData: ReqDataMap = null): DataRequestAction {
            return createAutoRefreshAction(ListRefreshForward.BOTTOM_REFRESH, service, reqData)
        }

        fun createTopMoreAction(service: String, reqData: ReqDataMap = null): DataRequestAction {
            return createAutoRefreshAction(ListRefreshForward.TOP_REFRESH, service, reqData)
        }

        /**
         * 手动下拉刷新action
         */
        fun createTopRefreshAction(service: String, reqData: ReqDataMap = null): DataRequestAction {
            return createClickRefreshAction(ListRefreshForward.TOP_REFRESH, service, reqData)
        }

        private fun safeGetService(service: String): String {
            return if (service.startsWith("/")) service else "/${service}"
        }

    }
}
