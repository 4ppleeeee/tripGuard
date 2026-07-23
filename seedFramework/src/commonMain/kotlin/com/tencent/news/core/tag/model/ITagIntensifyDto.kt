package com.tencent.news.core.tag.model

import com.tencent.news.core.parcel.IKmmParcelable

interface ITagIntensifyDto : IKmmParcelable {

    var intensifyType: String       // tag的增强类型文案：精选tag、创作者活动tag、工具型tag等

    val intensifyShowType: String   // 0=不显示type文案和分割, 1= type文案 + " | "

}