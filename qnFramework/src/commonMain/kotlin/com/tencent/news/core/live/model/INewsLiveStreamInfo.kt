package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnStreamInfo = @Serializable(INewsLiveStreamInfo.QnSerializer::class) INewsLiveStreamInfo

/**
 * 直播流信息接口
 * 对应 JSON 字段：
 * {
 *   "bit_rate": "0",           // 比特率
 *   "desc": "",                // 描述
 *   "hls_url": "https://...",  // HLS 播放地址
 *   "flv_url": "https://...",  // FLV 播放地址
 *   "rtmp_url": "rtmp://...",  // RTMP 播放地址
 *   "h5_url": ""               // H5 播放地址
 * }
 */
interface INewsLiveStreamInfo : IKmmKeep, IKmmParcelable {
    var bit_rate: String // 比特率
    var desc: String // 描述
    var hls_url: String // HLS 播放地址
    var flv_url: String // FLV 播放地址
    var rtmp_url: String // RTMP 播放地址
    var h5_url: String // H5 播放地址

    object QnSerializer : QnInterfaceSerializer<INewsLiveStreamInfo>(INewsLiveStreamInfo::class)

    companion object : IQnInterfaceCreator<INewsLiveStreamInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}

