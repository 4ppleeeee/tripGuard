package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import kotlinx.serialization.Serializable


@Suppress("AnnotationOnSeparateLine")
typealias QnTagColumnAttr = @Serializable(ITagColumnAttr.QnSerializer::class) ITagColumnAttr

interface ITagColumnAttr : IKmmKeep {
    var isFree: Int             // 需要付费【新增】 0 付费 1免费
    var isEnd: Int              // 完结状态
    var isSelected: Int         // 是否精选

    var planedDocsCount: Int        // 章节数
    var updateCount: Int            // 已更新章节数
    var plannedPaidDocsCount: Int   // 付费章节数

    var price: Int              // 专栏价格
    var discountPrice: Int      // 折扣价格，单位钻石
    var discountStatus: Int     // 0 未配置折扣信息, 1 配置了折扣信息
    var singleArticlePrice: Int         // 单篇价格
    var discountSingleArticlePrice: Int // 打折后单篇价格

    var discountStartTime: Long // 开始时间，单位时间戳
    var discountEndTime: Long   // 截止时间，单位时间戳

    fun hasValidDiscountActivity(): Boolean // 是否有有效的折扣活动

    object QnSerializer : QnInterfaceSerializer<ITagColumnAttr>(ITagColumnAttr::class)

    companion object : IQnInterfaceCreator<ITagColumnAttr> {
        override fun defaultSerializer() = QnSerializer
    }

}