package com.tencent.news.core.compose.scaffold.share

import com.tencent.news.core.compose.scaffold.modifiers.QnViewDtElementIds
import com.tencent.news.core.dt.constants.IDtElementId
import com.tencent.news.core.share.IShareChannel
import com.tencent.news.core.share.api.ShareChannel

fun getPostSharePanelElementId(shareChannel: IShareChannel): IDtElementId {
    return when (shareChannel.channel) {
        ShareChannel.SCREENSHOT -> QnViewDtElementIds.ShareScreenPanel
        else -> QnViewDtElementIds.ShareCardPanel
    }
}
