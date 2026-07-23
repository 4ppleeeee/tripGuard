@file:Suppress("PrivatePropertyName", "ConstructorParameterNaming")

package com.tencent.news.core.page.biz.column

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tag.model.QnTagInfo
import kotlinx.serialization.Serializable


@Serializable
data class ColumnGift(
    private val has_gift: Boolean = false,  // 是否已拥有该礼物权益
    val icon: String? = "",                 // 礼物图标
    val desc: String? = "",                 // 礼物描述
    val link: ColumnLink? = null,           // 链接
    val tagInfo: QnTagInfo? = null,      // 专栏信息
    val type: Int? = 0,                     // 0: 赠送服务 1：优惠折扣倒计时
) : IKmmKeep {
    val hasGift: Boolean
        get() = has_gift
}


@Serializable
data class ColumnLink(
    var icon: String? = "",
    var desc: String? = "",
    var desc_icon_font: String? = "",
    var url: String? = "",
) : IKmmKeep {
    val descIconFont: String?
        get() = desc_icon_font
}
