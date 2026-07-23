package com.tencent.news.core.compose.share

import com.tencent.news.core.dt.constants.DtCardPanelType
import com.tencent.news.core.dt.constants.DtElementId

data class ShareDtParams(
    val elementId: DtElementId,
    val cardPanelType: String = DtCardPanelType.DEFAULT,
    val eType: ShareDtEType? = null,
)
