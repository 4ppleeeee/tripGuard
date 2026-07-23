package com.tencent.news.core.tads.fullscreenmask.config

import com.tencent.news.core.page.model.StructLottie
import com.tencent.news.core.platform.api.getShiplyFloat

object AdFullScreenMaskConfig {

    @Suppress("MaxLineLength")
    fun getCloseBtnLottieUrl(): StructLottie {
        return StructLottie(
            urlAndroid = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20241011102658/Production/qn_group_fucengad_shut.lottie",
            urlIOS = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20241011102658/Production/qn_group_fucengad_shut.zip"
        )
    }

    fun getOffsetY(height: Int): Float {
        return height * getShiplyFloat("ad_full_screen_mask_offset_y_percent")
    }
}