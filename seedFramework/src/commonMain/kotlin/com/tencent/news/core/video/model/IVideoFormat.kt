package com.tencent.news.core.video.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 视频格式接口，描述视频的清晰度和文件大小信息
 */
interface IVideoFormat : IKmmParcelable, IKmmKeep {
    var fs: Long        // 文件大小（字节）
    var name: String    // 清晰度名称（如 "sd", "hd", "shd", "fhd"）
}
