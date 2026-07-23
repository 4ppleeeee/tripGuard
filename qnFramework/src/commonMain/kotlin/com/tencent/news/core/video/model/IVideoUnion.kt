package com.tencent.news.core.video.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

interface IVideoUnion : IKmmParcelable, IKmmKeep {

    val isScreeningId: String?      // 是否为点映礼
    val downright: String?          // 下载权限
    val defn: String?               // 清晰度列表

    val longVideoScene: String?     // 使用场景

    val headTimeSec: String?        // 片头时长 s
    val tailTimeSec: String?        // 片尾时长 s

    val vipPayMode: String          // 付费模式，目前控制点映礼的ui；

    val svipAdvance: String

    fun getVipPayAct(): IVideoPlayAct?  // 点映礼集合

    fun getSpecialContentOperationTypeId(): List<String>?

}
