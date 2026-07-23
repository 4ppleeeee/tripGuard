@file:Suppress("PropertyName", "PrivatePropertyName")

package com.tencent.news.core.tads.model.interact

import com.tencent.news.core.extension.isNotNullOrBlank
import com.tencent.news.core.tads.constants.AdLoid


interface IAdBrokenCreativeInfo {

    val materialAppearTime: Long

    val brokenVideoList: List<IAdBrokenVideoInfo>

    val redundantSize: Long // 冗余放大尺寸，防止素材制作不精细，露出背景内容

    var curPlayPosition: Long

}


interface IAdBrokenVideoInfo {

    val videoUrl: String

    val videoMd5: String

}

fun IAdBrokenCreativeInfo?.isDataValid(loid: Int): Boolean {
    return this?.brokenVideoList?.firstOrNull()?.videoUrl.isNotNullOrBlank() &&
            loid != AdLoid.VERTICAL_VIDEO
}