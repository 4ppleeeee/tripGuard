package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


// 端智能sdk用到的广告位置信息

@Suppress("SerialNameAtPublicClass")
@Serializable
class AdPosInfo : IKmmKeep {

    @SerialName("pos_id")
    var posId: Long = 0            // ams广告位id

    @SerialName("position_scene")
    var positionScene: Int = 0     // 广告位三级场景数据，car policy返回

}