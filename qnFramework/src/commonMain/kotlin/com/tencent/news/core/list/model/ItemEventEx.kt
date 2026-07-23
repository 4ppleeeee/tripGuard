package com.tencent.news.core.list.model

import com.tencent.news.core.extension.getNonNull

object ItemEventEx {

    val IListItem?.eventId
        get() = this?.eventDto?.hotEvent?.baseDto?.cmsId.getNonNull()

}