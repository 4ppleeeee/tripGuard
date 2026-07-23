package com.tencent.news.core.tag.model


// 不同尺寸封面图（目前用于 付费专栏简介和会员简介里的图片）
interface ITagLeadPicData {
    val pic1000SizeData: ITagPicData?
    val pic640SizeData: ITagPicData?
    val pic641SizeData: ITagPicData?
    val picOriginalData: ITagPicData?
}