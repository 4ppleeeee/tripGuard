package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.isIOSPlatform


// todo genesisli opt 接口太大了，应该拆分下，比如云游戏拆出去

interface IGameDownloadInfo : IKmmKeep {
    val appId: String           // 应用ID
    val packageName: String     // 包名
    val packageVersion: Int     // 版本
    val apkUrl: String          // apk下载地址
    val iosUrl: String          // ios app store地址
    val hmPkgPrefix: String     // 鸿蒙应用包名（鸿蒙应用市场跳转用，后台字段 hm_pkg_prefix）
    val editorIntro: String     // 编辑推荐语
    val isCloudGameDataValid: Boolean      // 是否是云游戏
    val qqAppId: String
    val wxAppId: String
    val cloudGameId: String
    val sdkDirection: String
    val sdkBgImg: String
    val cloudPlayUrl: String
    val downloadBgUrl: String
    val downloadBtnUrl: String
    val downloadPageUrl: String
    val externalLogin: Boolean
    val playWhileDownSwitch: Boolean
    val qqToken: String
    val sidebarText: String
    val dlBtnText: String
    val isSupportQuicklyUpdate: Boolean
}

object IGameDownloadInfoEx {

    fun IGameDownloadInfo?.isDataValid(): Boolean {
        this ?: return false
        if (isIOSPlatform()) {
            return iosUrl.isNotEmpty()
        }
        return apkUrl.isNotEmpty() && packageName.isNotEmpty() && packageVersion > 0
    }

}