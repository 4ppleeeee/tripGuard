package com.tencent.news.core.compose.platform.args

import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.list.model.IKmmFeedsItem

interface IStructPageArgs : IStructPageBaseArgs {
    val feedsItem: IKmmFeedsItem    // 页面Item
}

interface IStructPageBaseArgs : IComposePageArgs {
    val channelId: String           // 频道id
    val schemeFrom: String?         // 拉起渠道
    val originScheme: String?       // 拉起scheme
}