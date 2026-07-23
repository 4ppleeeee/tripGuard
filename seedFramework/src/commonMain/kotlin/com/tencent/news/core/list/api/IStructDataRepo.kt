@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.list.api

import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.constants.ListRefreshActionData
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.list.constants.isCloudReplaceAction
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.DataRequest
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.platform.api.INetworkParser
import com.tencent.news.core.platform.api.NetworkBuilder
import com.tencent.news.core.tads.constants.AdLoid
import com.tencent.news.qnchannel.api.IChannelInfo


data class StructDataEnv constructor(
    val refreshForward: ListRefreshForward,
    val refreshAction: ListRefreshAction,
    val refreshActionData: ListRefreshActionData?,
    val channelInfo: IChannelInfo,
    val pageItem: IKmmFeedsItem?,
    val anchorTabId: String?,
) {
    var resetByTime: Boolean = false

    var pageNum: Int = 0 // 默认值和 currentRequestPage 保持一致

    fun isStructChannel(): Boolean = (pageItem == null)

    fun isReset(): Boolean = refreshForward == ListRefreshForward.RESET

    /**
     * 判断是否是手动下拉刷新
     */
    fun isPullDownRefresh(): Boolean {
        return refreshForward == ListRefreshForward.TOP_REFRESH && refreshAction == ListRefreshAction.PULL_DOWN
    }

    fun updatePageNum(currentRequestPage: Int) {
        if (refreshAction.isCloudReplaceAction()) {
            this.pageNum = currentRequestPage - 1
        } else {
            this.pageNum = currentRequestPage
        }
    }
}

private typealias Net = NetworkBuilder<*>

interface IStructDataRepo {

    // 不需要网络请求，直接本地构建首页数据的情况，用这个（会代替 createResetRequest）
    fun createLocalResetPageWidget(): StructPageWidget? = null

    // 可根据本次请求环境构建本地 reset 数据；默认兼容旧的无参实现
    fun createLocalResetPageWidget(dataEnv: StructDataEnv): StructPageWidget? = createLocalResetPageWidget()

    // 页面首刷请求
    fun createResetRequest(defaultRequest: DataRequest, dataEnv: StructDataEnv): Net

    // 其他分页请求（一般不需要额外实现这个；普遍是首刷伪造load_more 等action来处理分页）
    fun createOtherRequest(defaultRequest: DataRequest, dataEnv: StructDataEnv): Net? = null

    // 预加载请求：当预加载要替换接口时，实现这个，如果返回非空会优先使用它
    fun createPreloadRequest(defaultRequest: DataRequest, dataEnv: StructDataEnv): Net? = null

    // 主广告位 loid 值
    fun getMajorAdLoid(): Int = AdLoid.NONE

    // true：使用 appNetwork().jsonPostRequest 发送请求
    // false：使用 appNetwork().postRequest 发送请求（form格式）
    fun useJsonPost(): Boolean? = null

    // 是否校验 ret!=0 当作接口错误
    fun checkRet(): Boolean? = null

    // 是否禁用 forceUrlParams（chlid/page/forward 强制加到 URL 上的逻辑）
    // 部分接口（如 /gw/long_video/list_selected）后端会优先读 URL 上的 page 参数，
    // 与 body 中的 page 冲突导致报错，需要禁用
    fun disableForceUrlParams(): Boolean = false

    /**
     * 拦截页面返回的json处理：
     * - 如果需要进行数据结构转换，则在这里做；
     * - 如果后台直接返回的就是结构化协议，这里可以返回null
     */
    fun buildStructPageWidgetWithJson(
        dataEnv: StructDataEnv,
        originJson: String,
    ): StructPageWidget? = null

}

interface IStructDataLocalRepo : IStructDataRepo {
    // 不需要网络请求，直接本地构建首页数据的情况，用这个（会代替 createResetRequest）
    override fun createLocalResetPageWidget(): StructPageWidget

    // 本地情况下，这个没用了，不需要调用；给个默认实现即可
    override fun createResetRequest(defaultRequest: DataRequest, dataEnv: StructDataEnv): Net =
        defaultRequest.buildDefaultNetworkBuilder(dataEnv)
}

fun IStructDataRepo.getLogKey(): String = this::class.simpleName.toString()

typealias StructPageNetworkBuilder = NetworkBuilder<StructPageWidget>

fun DataRequest.buildDefaultNetworkBuilder(
    dataEnv: StructDataEnv,
    parser: INetworkParser<StructPageWidget>? = null
): StructPageNetworkBuilder {
    return NetworkBuilder(
        url = buildRequestUrl(dataEnv.channelInfo),
        parser = parser,
        params = reqdata
    )
}
