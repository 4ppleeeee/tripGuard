package com.tencent.news.core.list.api

typealias FlexCreateCallback = (flexCtrl: IFlexibleFeedsController) -> Unit

interface IFlexFeedsControllerHolder {

    // 常规情况会创建并固定持有一个ctrl；
    // 但是，当 isStatusChanged 发生变化时，再次获取会自动重建
    fun createOrGet(
        checkReCreate: Boolean = false,
        onCreate: FlexCreateCallback? = null
    ): IFlexibleFeedsController?

    // 影响列表数据的核心状态发生变化，需要触发重建（例如：切换个性化开关）
    // 这个判断方法只代表应该触发重建，实际的重建操作需要等再次调用 createOrGet 时执行
    fun needResetForStatusChanged(): Boolean

}