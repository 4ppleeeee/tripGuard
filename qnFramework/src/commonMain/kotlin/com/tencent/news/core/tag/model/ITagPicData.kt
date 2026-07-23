package com.tencent.news.core.tag.model


interface ITagPicData {
    var imageUrl: String?   // 图片链接
    var height: Int         // 高度
    var width: Int          // 宽度
}

fun ITagPicData?.isValid(): Boolean {
    this ?: return false
    // 检查 url 是否有效
    val isUrlValid = !imageUrl.isNullOrEmpty()
    return isUrlValid && width > 0 && height > 0
}