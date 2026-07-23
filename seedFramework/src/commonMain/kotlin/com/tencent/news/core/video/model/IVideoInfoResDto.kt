package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IVideoInfoResDto : IKmmParcelable {
    var coverImg: String        // 封面图
    var coverBigImg: String     // 封面大图（用的比较少）
    var firstFramePic: String   // 首帧图
    var subtitleModel: Int      // 字幕模型类型

    var coverWidth: Int         // 封面图宽高（360x640）
    var coverHeight: Int
    var videoWidth: Int         // 视频宽高（1080x1920）
    var videoHeight: Int

    val defaultResolution: String   // 默认清晰度

    var showType: Int  // 视频展示类型，0：普通 1：正方形

    var aspect: Float

    val videoSize: IVideoSize?  // 视频真实显现区域

    val blackBorder: IVideoBlackBorder?  // 黑边信息

    val formatList: List<IVideoFormat>?  // 视频格式列表（清晰度/文件大小）

}