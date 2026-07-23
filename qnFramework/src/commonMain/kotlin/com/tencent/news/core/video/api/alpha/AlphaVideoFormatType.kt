package com.tencent.news.core.video.api.alpha

/**
 * 透明视频格式类型（平台无关枚举）
 *
 * 透明视频通过特殊的像素排列方式将 RGB 和 Alpha 通道编码在同一视频中。
 * 各平台需在适配层将此枚举映射为平台 SDK 对应的格式常量。
 *
 * @param value 整数标识，用于跨平台序列化和鸿蒙侧映射
 */
enum class AlphaVideoFormatType(val value: Int) {

    /**
     * 左色右Alpha对齐（推荐）
     *
     * 每行像素：[RGB_A | Alpha_A | RGB_B | Alpha_B | ...]
     * 左半部分为 RGB 数据，右半部分为对应的 Alpha 数据，宽度对齐。
     */
    ALIGNED(0),

    /**
     * 左色右半Alpha（2:1）
     *
     * 每行像素：[RGB | Half_Alpha | RGB | Half_Alpha | ...]
     * RGB 宽度是 Alpha 宽度的 2 倍，Alpha 取相邻像素平均。
     */
    RGB_ALPHA_2_1(1),

    /**
     * 左Alpha右色（1:1）
     *
     * 每行像素：[Alpha | RGB | Alpha | RGB | ...]
     * Alpha 和 RGB 交错排列，1:1 对应。
     */
    ALPHA_RGB_1_1(2);

    companion object {
        fun fromValue(value: Int): AlphaVideoFormatType {
            return entries.firstOrNull { it.value == value } ?: ALIGNED
        }
    }
}
