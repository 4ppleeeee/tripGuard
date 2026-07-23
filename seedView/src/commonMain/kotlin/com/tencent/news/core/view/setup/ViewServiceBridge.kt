package com.tencent.news.core.view.setup

import androidx.compose.runtime.Composable
import com.tencent.news.core.compose.share.PostPreviewData
import com.tencent.news.core.compose.share.ShareResult
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.IKmmShareData
import com.tencent.news.core.share.api.ShareChannel
import com.tencent.news.core.share.model.IShareContent


/**
 * qnView 服务桥接器
 *
 * 将 qnView 对 qnCore Service 层（UserService、FeedsService、AdService）的依赖
 * 统一收口到此处，由 qnCore 在模块初始化时注入实现，避免 qnView 直接依赖 qnCore
 */
object ViewServiceBridge {

    lateinit var impl: IViewServiceBridge
        private set

    fun register(bridge: IViewServiceBridge) {
        impl = bridge
    }

}

typealias ViewCreator = @Composable () -> Unit

/**
 * 桥接接口定义，由 qnCore 实现并注册
 */
interface IViewServiceBridge {

    // 海报预览的默认本地资源
    @Composable
    fun createInitialPreviewDataWithPlaceholder(shareData: IKmmShareData): PostPreviewData

    // 展示海报预览页
    @Composable
    fun ShowPostPreviewComponent(
        previewData: PostPreviewData,
        shareData: IKmmShareData,
        onPosterClick: (ShareResult.CONTINUE) -> Unit,
    )

    // 创建分享渠道
    fun createShareChannel(shareChannel: ShareChannel): IShareChannel

    // 创建图片分享渠道
    fun buildImageShareContent(
        imagePath: String,
        shareData: IKmmShareData?,
        channel: ShareChannel?,
    ): IShareContent

    // 构建分享内容
    fun buildPageShareContent(shareData: IKmmShareData, channel: ShareChannel): IShareContent?

    fun fetchShareMetaData(shareData: IKmmShareData)
    fun createShareMetaData(shareData: IKmmShareData): Pair<String, String>?

    fun defaultLoadingView(): ViewCreator?
    fun defaultErrorView(): ViewCreator?
    fun defaultEmptyView(): ViewCreator? // 预留，这个还没实现

}
