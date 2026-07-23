package com.tencent.news.qnchannel.api

import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IKmmShareInfo
import com.tencent.news.core.tag.model.QnTagInfo
import kotlinx.coroutines.flow.MutableStateFlow


interface IChannelEnv {

    // 数据上报归因用的频道：在二级频道上等同于channelKey；在页面子tab上是来源的频道id
    var newsChannel: String

    var channelType: String // 用来进一步区分频道的二级key，有时候也用来标识上报的二级频道（类似 newsChannel）

    // 底层页文章item
    var pageItem: IKmmFeedsItem?
    
    var pageArgs: IComposePageArgs?

    // 频道 FlexController 重建key
    var rebuildStatusKey: String?

    // 专题传递给fragment的分享信息
    var shareData: IKmmShareInfo?
    
    // 专题传递给fragment的分享信息（MutableState 版本，用于响应式更新）
    var shareDataState: MutableStateFlow<IKmmShareInfo?>
    
    // 标签信息（MutableState 版本，用于响应式更新）
    var tagInfoState: MutableStateFlow<QnTagInfo?>

}