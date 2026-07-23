package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable

// 非后台下发，本地绑定的参数：
interface IVideoInfoContextDto : IKmmParcelable {
    var syncCoverPic: String        // 长视频使用，剧集对应的封面图
    var isFromAutoPlay: Boolean     // 是否来自于自动播放
    var videoAlbumIndex: Int        // 所属专辑index，仅本地使用。-1标示没有对应专辑
}