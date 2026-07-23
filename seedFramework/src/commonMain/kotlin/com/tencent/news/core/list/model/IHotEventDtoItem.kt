package com.tencent.news.core.list.model

import com.tencent.news.core.extension.ICmsModelDtoItemDoc

interface IHotEventDtoItem : ICmsModelDtoItemDoc {
    val baseDto: IHotEventBaseDto
    val ugcDto: IHotEventUgcDto
    val eventCtxDto: IHotEventContextDto
}