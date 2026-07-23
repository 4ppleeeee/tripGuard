package com.tencent.news.core.tads.extension

import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.isIOSPlatform
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.platform.api.isAppBrowserMode
import com.tencent.news.core.tads.constants.AdActType
import com.tencent.news.core.tads.model.IAppChannelInfoEx.isDataValid
import com.tencent.news.core.tads.model.IKmmAdOrder

fun IKmmAdOrder?.isDownloadDataValid(): Boolean {
    this ?: return false
    return isDownloadOrder() &&
            !isAppBrowserMode() &&
            downloadDto.pkgUrl.isNotNullOrEmpty() &&
            downloadDto.appChannelInfo.isDataValid()
}

/**
 * 是否为下载类订单
 */
fun IKmmAdOrder?.isDownloadOrder(): Boolean {
    val actType = this?.info?.actType ?: return false
    if (isIOSPlatform()) {
        return actType == AdActType.DOWNLOAD
    }
    if (isHarmonyPlatform()) {
        return if (getShiplySwitch("ohos_open_app_as_download", true)) {
            return actType in listOf(AdActType.DOWNLOAD, AdActType.OPEN_APP)
        } else {
            actType == AdActType.DOWNLOAD
        }
    }

    val isActTypeValid = actType in listOf(AdActType.DOWNLOAD, AdActType.OPEN_APP)
    val isDownloadType = downloadDto.isGdtDownload || downloadDto.pkgUrl.isNotEmpty()
    val isDownloadDataValid = downloadDto.pkgName.isNotEmpty() && downloadDto.pkgVersion > 0
    return isDownloadType && isDownloadDataValid && isActTypeValid
}