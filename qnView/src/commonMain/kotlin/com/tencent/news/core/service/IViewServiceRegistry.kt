package com.tencent.news.core.service

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.compose.scaffold.card.IFeedsItemCardService
import com.tencent.news.core.compose.share.IShareComponentRegistry
import com.tencent.news.core.extension.IServiceDoc
import com.tencent.news.core.setup.ILazyImplHolder
import com.tencent.news.core.setup.LazyImpl

// compose UI组件相关服务
interface IViewServiceRegistry : IServiceDoc {

    val itemCard: IFeedsItemCardService         // 信息流卡片
    val layer: IStructLayerRegistry             // 浮层、弹窗
    val titleBar: IStructTitleBarRegistry       // 页面顶部导航
    val header: IStructHeaderRegistry           // 页面头部
    val bottomBar: IStructBottomBarRegistry     // 页面底部导航条
    val channelBar: IStructChannelBarRegistry   // 多tab导航条
    val hanging: IStructHangingRegistry         // 悬停区
    val btn: IStructBtnRegistry                 // 各种按钮（可配合 TitleBar、BottomBar、Layer使用）
    val channel: IStructChannelRegistry         // 频道、子tab
    val share: IShareComponentRegistry          // 分享弹窗

    companion object : ILazyImplHolder<IViewServiceRegistry> {
        @KmmInternalApi
        override lateinit var instance: LazyImpl<IViewServiceRegistry>
    }

}