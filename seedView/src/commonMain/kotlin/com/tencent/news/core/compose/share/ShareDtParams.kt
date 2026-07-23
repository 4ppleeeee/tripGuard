package com.tencent.news.core.compose.share

import com.tencent.news.core.dt.constants.DtCardPanelType
import com.tencent.news.core.dt.constants.IDtElementId

data class ShareDtParams(
    val elementId: IDtElementId,
    val cardPanelType: String = DtCardPanelType.DEFAULT,
    val eType: ShareDtEType? = null,
)
