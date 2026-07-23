package com.tencent.news.core.platform.api.export

import com.tencent.news.core.app.constants.DensityScaleGradient

// 需要ArkTs注入的字段，都是基础类型
@Suppress("MaxLineLength")
interface IExportAppStatus {

    // 【app基础信息】
    fun getVersion(): Int = 0               // app版本号，4位数字格式的，例如：7730
    fun getVersionName(): String = ""       // app版本号，格式类似："1.0.0"
    fun getAppName(): String = ""
    fun getAppBuildNo(): String = "0"       // 流水线构建号
    fun getQQAppId(): String = ""           // 接入 QQ sdk 的 appId
    fun getWxAppId(): String = ""           // 接入 微信sdk 的 appId

    // 【设备身份标识】：
    fun getDtSessionId(): String // 大同公参：dt_ussn，【口径】：https://tapd.woa.com/tfl/captures/2024-05/tapd_10045201_base64_1716607531_780.png
    fun getQIMEI36(): String
    fun getOAID(): String
    fun getTOAID(): String = "" // 图灵盾 OAID（Turing OAID）
    fun getTAID(): String
    fun getDevId(): String

    // 【debug】相关：
    fun isDebug(): Boolean = false                  // 是否是debug包
    fun isRdmDebug(): Boolean = false               // 是否是RDM包
    fun isGrey(): Boolean = false                   // 是否是灰度包（安卓能区分，iOS不行）
    fun isIntegrationMode(): Boolean = false        // 是否开启了集成测试模式（用于一些调试逻辑判断）

    // 【应用内各种模式切换】：
    fun isTalkbackEnabled(): Boolean = false        // 无障碍语音阅读模式
    fun isBrowseMode(): Boolean = false             // 仅浏览模式
    fun currentTextScaleGradient(): DensityScaleGradient = DensityScaleGradient.L1 // 文字缩放梯度
    fun isNightMode(): Boolean = false              // 夜间模式
    fun isInReviewMode(): Boolean = false
    fun isTextMode(): Boolean = false               // 文字模式
    fun isPersonalizedSwitchOpen(): Boolean = true  // 个性化推荐开关（注意：默认是打开的）
    fun isInNewsTopManualMode(): Boolean = false    // 要闻主编精选模式
}
