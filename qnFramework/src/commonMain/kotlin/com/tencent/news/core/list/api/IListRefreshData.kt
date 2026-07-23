package com.tencent.news.core.list.api

import com.tencent.news.core.list.model.IKmmFeedsItem


interface IListRefreshData {

    fun getResultCode(): String? = "0"      // 返回码：0-成功

    fun getFeedsList(): List<IKmmFeedsItem?>?           // 文章数据列表
    fun getExtraList(): List<IKmmFeedsItem?>? = null    // 额外文章列表（目前用于给tab2预加载的文章数据）

    fun getRefreshTimestamp(): Long = 0     // 刷新时间戳（后台的）
    fun getRefreshWording(): String? = ""   // 刷新文案
    fun getLoadedFinishText(): String? = ""   // 加载完成的尾部文案

    fun disableNewsReplace(): String? = "" // 是否禁止云重重排

    fun hasMore(): Boolean = false          // 是否能继续翻页
    fun getListTransParam(): String? = ""   // 列表翻页透传信息

    fun getPrivacyPopupTime(): Int = 0      // 仅浏览模式下，首页隐私协议弹窗1自动消失延迟时长

    fun getDetailPrivacyPopupTime(): Int = 0      // 仅浏览模式下，底层页隐私协议弹窗1自动消失延迟时长

    fun getListPageInfo(): IListPageIndexInfo? = null // 详细的翻页信息

    @Deprecated("老逻辑，待删")
    fun getPageNum(): Int = 0

    @Deprecated("老逻辑，待删")
    fun getNextUpdateNum(): Int = 0

}


/**
 * 本地置顶的文章列表，插入时序在广告处理完成之后（目前主要用于'上次看到这里'功能）
 */
interface ILocalFixTopList {

    var localFixTopList: List<IKmmFeedsItem>?

}