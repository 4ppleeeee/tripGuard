package com.tencent.news.core.tag.model

import com.tencent.news.core.list.model.QnKmmFeedsItem

/**
 * 标签头部数据协议
 */
interface ITagHeaderProtocol {
    val code: Int
    val msg: String?
    val data: ITagHeaderData?
    val color_gray: Int
}

/**
 * 标签头部数据内容协议
 */
interface ITagHeaderData {
    val basic: QnTagInfo?
    val customizeTagInfos: List<ITagInfoItem>?
    val headerItem: QnKmmFeedsItem?
}

/**
 * 标签信息项协议
 */
interface ITagInfoItem {
    val basic: QnTagInfo?
}