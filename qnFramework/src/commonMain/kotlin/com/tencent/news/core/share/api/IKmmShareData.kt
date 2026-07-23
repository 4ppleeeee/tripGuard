package com.tencent.news.core.share.api

import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.tads.model.IKmmAdOrder
import com.tencent.news.core.vm.IAiShareMetadataRepoStub
import com.tencent.news.core.vm.IDetailModelStub


interface IKmmShareData {

    /**
     * 文章信息
     */
    val item: IKmmFeedsItem?

    /**
     * 信息流数据
     */
    val items: List<IKmmFeedsItem>?

    /**
     * 图文信息
     */
    val sourceNewsDetail: IDetailModelStub?

    /**
     * 信息流广告
     */
    val originAd: IKmmAdOrder?

    /**
     * 频道
     */
    val channelId: String?

    /**
     * 可选项
     */
    val option: IKmmShareDataOption?
}


interface IKmmShareDataOption {

    /**
     * 只显示第一行
     */
    val onlyFirstLine: Boolean

    /**
     * 是否不安全评论
     */
    val isUnSafeComment: Boolean

    /**
     * 优先使用shareTitle,shareContent等
     */
    val useShareInfoFirst: Boolean

    /**
     * 分享到weixin/qq的兜底图片url
     */
    val imageWeiXinQqUrls: Array<String?>?

    /**
     * 分享到微博/qzone的兜底图片url
     */
    val imageWeiBoQZoneUrls: Array<String?>?

    /**
     * 分享场景url
     */
    val sharePos: String

    /**
     * 是否显示海报预览面板
     * 默认为false，早晚报分享场景为true
     */
    val showPosterPreview: Boolean

    /**
     * 分享场景
     * 默认为DEFAULT
     */
    val shareScene: ShareSceneType

    /**
     * aigc的meta信息
     */
    val aiMetadata: IAiShareMetadataRepoStub?

    /**
     * 海报分享类型：比如早报海报，频道海报样式不同
     */
    val posterShareChannel: ShareChannel?

    /**
     * 视频文件的真实下载地址（用于保存视频到相册）
     * 注意：与 shareUrl 不同，shareUrl 是分享落地页链接，videoUrl 是视频文件的 mp4 地址
     */
    val videoUrl: String
}


class KmmShareDataOptionBuilder : IKmmShareDataOption {
    override val onlyFirstLine: Boolean = false
    override var isUnSafeComment: Boolean = false
    override var useShareInfoFirst: Boolean = false
    override var imageWeiXinQqUrls: Array<String?>? = null
    override var imageWeiBoQZoneUrls: Array<String?>? = null
    override var sharePos: String = ""
    override var showPosterPreview: Boolean = false
    override var shareScene: ShareSceneType = ShareSceneType.DEFAULT
    override var aiMetadata: IAiShareMetadataRepoStub? = null
    override val posterShareChannel: ShareChannel? = null
    override var videoUrl: String = ""
}
