package com.tencent.news.core.tads.tab2.vm

// 模板大卡，颜色样式
data class AdBigCardColorStyle(
    val cardBgColor: Long,              // 大卡背景色
    val cardBgHighlightAlpha: Float,    // 大卡背景高亮透明度

    val titleColor: Long,               // 主标题颜色
    val descColor: Long,                // 描述信息颜色

    val dividerColor: Long,             // 分割线颜色
    val closeBtnColor: Long,            // 关闭按钮颜色

    val labelTextColor: Long,           // 标签字色
    val labelBgColor: Long,             // 标签背景色
) {
    companion object {
        val White = AdBigCardColorStyle(
            cardBgColor = 0xFFFFFFFF,
            cardBgHighlightAlpha = 1.0f,

            titleColor = 0xFF333333,
            descColor = 0xFF5C5C5C,

            dividerColor = 0x0D000000,
            closeBtnColor = 0xFF5C5C5C,

            labelTextColor = 0xff999999,
            labelBgColor = 0x0D000000,
        )

        val Dark = AdBigCardColorStyle(
            cardBgColor = 0x66333333,
            cardBgHighlightAlpha = 0f,

            titleColor = 0xFFFFFFFF,
            descColor = 0xFFFFFFFF,

            dividerColor = 0x0DFFFFFF,
            closeBtnColor = 0xBFFFFFFF,

            labelTextColor = 0xFFFFFFFF,
            labelBgColor = 0x33999999,
        )
    }
}