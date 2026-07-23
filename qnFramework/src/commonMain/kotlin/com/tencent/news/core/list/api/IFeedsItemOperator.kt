package com.tencent.news.core.list.api

import com.tencent.news.core.list.model.IKmmFeedsItem


typealias ItemCursor = (IKmmFeedsItem) -> Boolean


interface IFeedsItemOperator {

    // 【增】支持插入多个item
    fun insertFeedsItem(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Boolean

    // 【增】支持插入多个item到cursor位置之后
    fun insertFeedsItemAfter(newData: List<IKmmFeedsItem>, cursor: ItemCursor): Boolean

    // 【增】追加到最后
    fun appendFeedsItem(newData: List<IKmmFeedsItem>): Boolean

    // 【删】支持同时删除多个item
    fun removeFeedsItem(cursor: ItemCursor): List<IKmmFeedsItem>

    // 【改】返回被替换的item（仅替换首个匹配的）
    fun replaceFeedsItem(newData: List<IKmmFeedsItem>, cursor: ItemCursor): IKmmFeedsItem?

    // 【查】找匹配的首个item（查询性能比 getAllFeedsItemList 后再find 强）
    fun findFeedsItem(cursor: ItemCursor): IKmmFeedsItem?

    // 【查】当前全部item列表
    fun getAllFeedsItemList(): List<IKmmFeedsItem>

}
