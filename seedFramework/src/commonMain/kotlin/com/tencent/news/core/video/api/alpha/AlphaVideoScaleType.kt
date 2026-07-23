package com.tencent.news.core.video.api.alpha

/**
 * 透明视频缩放模式（平台无关枚举）
 *
 * 各平台需在适配层将此枚举映射为平台 SDK 对应的 ScaleType。
 *
 * @param value 整数标识，用于跨平台序列化和鸿蒙侧映射
 */
enum class AlphaVideoScaleType(val value: Int) {

    /**
     * 填满容器并裁剪（默认）
     *
     * 保持视频比例，放大填满容器，超出部分裁剪。
     */
    CENTER_CROP(0),

    /**
     * 保持原始比例，居中显示
     *
     * 保持视频比例，可能留有黑边（透明边）。
     */
    FIT_CENTER(1);

    companion object {
        fun fromValue(value: Int): AlphaVideoScaleType {
            return entries.firstOrNull { it.value == value } ?: CENTER_CROP
        }
    }
}
