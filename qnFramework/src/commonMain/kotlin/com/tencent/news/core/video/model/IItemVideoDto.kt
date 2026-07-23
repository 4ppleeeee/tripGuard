package com.tencent.news.core.video.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IKmmVideoChannel
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Transient


interface IItemVideoDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    @Deprecated("直接用 videoDto")
    var videoChannel: IKmmVideoChannel? // todo genesisli opt: IKmmVideoChannel 后面考虑不对外暴露，就用 IVideoDto

    var videoInfo: IVideoInfo?          // 【重要】视频播放的核心数据

    val enableLike: Boolean             // 是否支持点赞

    val enableReplay: Boolean           // 视频播放完了，下次触发自动播是否重复播放

    var kkRecommendType: Int            // KK推荐类型
    val kkRecommendReason: String          // KK推荐理由

    @Transient
    var isVideoPlayComplete: Boolean    // 本地标记视频是否播完，配合 enableReplay 使用

    var coverTitle: String              // 压在封面上的主标题
    var coverSubTitle: String           // 压在封面上的副标题
    var hasVideo: String                // 是否有视频
    var videoTagInfo: String            // 长视频分类标签
    var disableVideoAutoPlay: Int       // 后台下发字段（6090实验），禁止部分内容自动播

}