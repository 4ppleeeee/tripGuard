package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc


interface IAdMdpaDto : IAdOrderDtoDoc {

    val showAsMdpa: Boolean

    val mdpaItemList: List<IAdMdpaItem>?

}