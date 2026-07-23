package com.tencent.news.core.platform.api

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.platform.QnPlatformLogic
import kotlinx.serialization.Serializable

interface IAppSkinManager {

    fun getRefreshHeaderSkin(channelId: String, isDarkTheme: Boolean): RefreshHeaderSkinStyle =
        RefreshHeaderSkinStyle()
}

@Serializable
data class RefreshHeaderSkinStyle(
    val refreshingBgColor: String = "",
    val refreshingTextColor: String = "",
    val refreshedBgColor: String = "",
    val refreshedTextColor: String = "",
    val lottieUrl: String = "",
) : IKmmKeep

fun skinManager(): IAppSkinManager? {
    return QnPlatformLogic.skinManager
}
