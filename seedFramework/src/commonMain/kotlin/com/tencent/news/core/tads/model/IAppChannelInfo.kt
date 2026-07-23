package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.isNotNullOrEmpty


// 应用下载‘十要素’

interface IAppChannelInfo : IKmmKeep {

//    val appName: String         // app中文名（@7540 不要求展示了）

    val authorName: String      // 运营商
    val versionName: String     // app版本
    val developerName: String   // 开发者
    val suitableAge: String     // 适用年龄

    val permissionsInfo: String // 应用权限
    val privacyInfo: String     // 隐私政策
    val featureInfo: String     // 功能介绍
    val icpInfo: String         // 备案信息（@7540 ‘备案号’‘备案单位’合并到一个url里展示）

}

object IAppChannelInfoEx {

    const val DIV = "  |  "

    const val PERMISSIONS_TEXT = "应用权限"
    const val PRIVACY_TEXT = "隐私政策"
    const val FEATURE_TEXT = "功能介绍"
    const val ICP_TEXT = "备案信息"

    const val PERMISSIONS_SHORT_TEXT = "权限"
    const val PRIVACY_SHORT_TEXT = "隐私"
    const val FEATURE_SHORT_TEXT = "功能"
    const val ICP_SHORT_TEXT = "备案"

    fun joinAppChannelInfo(vararg values: String): String {
        return values.filter { it.isNotEmpty() }
            .joinToString(separator = DIV)
    }

    fun compatIcpInfo(icpNum: String?, icpUnit: String?): String {
        return listOf(icpNum, icpUnit)
            .filter { !it.isNullOrEmpty() }
            .joinToString("\n")
    }


    fun IAppChannelInfo?.isDataValid(): Boolean {
        this ?: return false

        // 5个要素必备，备案号目前覆盖不全，暂不校验
        return authorName.isNotEmpty() &&
                versionName.isNotEmpty() &&
                privacyInfo.isNotEmpty() &&
                permissionsInfo.isNotEmpty() &&
                featureInfo.isNotEmpty()
    }

    // 游戏要求同时展示：运营商 和 开发者，都要校验下
    fun IAppChannelInfo?.isDataValid4Game(): Boolean =
        isDataValid() && this?.developerName.isNotNullOrEmpty()

}