package com.tencent.news.core.video.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IVideoSize : IKmmParcelable {
    val aspect: Float   // 视频原始宽高比
    val left: Float     // 视频真实显现区，左边距[0,1)
    val right: Float    // 视频真实显现区，右边距(0,1]
    val top: Float      // 视频真实显现区，上边距[0,1)
    val bottom: Float   // 视频真实显现区，下边距(0,1]

    /** 判断视频显现区域参数是否合法 */
    fun isLegal(): Boolean {
        if (right <= left) return false
        if (right > 1f) return false
        if (left < 0f) return false
        if (bottom <= top) return false
        if (bottom > 1f) return false
        if (top < 0f) return false
        return true
    }
}
