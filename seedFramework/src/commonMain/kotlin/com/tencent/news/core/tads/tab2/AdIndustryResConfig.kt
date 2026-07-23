package com.tencent.news.core.tads.tab2

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable


@Serializable
data class AdIndustryResConfig(
    val industryID: Int = 0,        // 行业短id
    val typeName: String = "",      // 左上角标签名称
    val typeDes: String = "",       // 左上角标签副标题
    val typeIcon: String = "",      // 左上角图标
    val mainColor: String = "",     // 主题色（按钮、价格 等）
    val secondColor: String = "",   // 辅助色（原价、背景 等）
) : IKmmKeep