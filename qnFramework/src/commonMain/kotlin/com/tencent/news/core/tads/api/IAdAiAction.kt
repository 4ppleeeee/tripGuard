package com.tencent.news.core.tads.api

import com.tencent.news.core.tads.model.IKmmAdFeedsItem


interface IAdFeedsAiAction {

    // 替换广告订单（【注意】可能在线程回调！！！）
    fun onReplaceAdItem(oldAdItem: IKmmAdFeedsItem, newAdItem: IKmmAdFeedsItem)

}