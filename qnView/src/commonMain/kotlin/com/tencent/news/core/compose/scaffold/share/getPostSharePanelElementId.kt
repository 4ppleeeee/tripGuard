package com.tencent.news.core.compose.scaffold.share

import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.ShareChannel

fun getPostSharePanelElementId(shareChannel: IShareChannel): DtElementId {
    return when (shareChannel.channel) {
        ShareChannel.SCREENSHOT -> DtElementId.ShareScreenPanel
        else -> DtElementId.ShareCardPanel
    }
}