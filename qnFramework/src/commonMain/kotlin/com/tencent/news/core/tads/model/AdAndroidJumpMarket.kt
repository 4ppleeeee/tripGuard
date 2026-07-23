package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmPure
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class AdAndroidJumpMarket : IKmmPure {

    @SerialName("market_deep_link")
    var marketDeepLink: String? = null

    @SerialName("market_package_name")
    var marketPackageName: List<String> = emptyList()

    @SerialName("market_jump_type")
    var marketJumpType = 0

    @SerialName("customized_deep_link")
    var marketDownloadDeepLink: String? = null                 // 厂商自动下载退回手动跳转厂商要先使用此字段，在有效期

    @SerialName("customized_atd_deep_link")
    var marketAtdDownloadDeepLink: String? = null               // 厂商下载，在有效期内使用

    @SerialName("customized_expired_time")
    var marketDownloadExpiredTime: Long = 0                      // 厂商下载scheme过期时间

    @SerialName("is_atd_not_report_charge")
    var isDisableMarketDownloadReport: Boolean = false           // 是否计费上报

    @SerialName("support_download_and_open")
    var isOpenAfterInstallSuccess: Boolean = false               // 厂商自动下载安装完成后是否直接打开
}

object AdJumpMarketType {

    const val TERMINAL: Int = 1  // 终端
    const val XIJING: Int = 2    // xj
}