package com.tencent.news.core.tads.constants

// 破窗挂件类型（目前 奥运画廊 在用）
enum class AdBrokenInfoType(val value: Int) {
    ITEM(1),    // 子item上的破窗挂件
    BOTTOM(2),  // 整个cell底部破窗
    TOP(3),     // 整个cell顶部破窗
}