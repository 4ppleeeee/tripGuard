package com.tencent.news.core.compose.platform


interface IComposePageSize {
    // 需要给kuikly传入初始化size，否则布局会异常的情况（例如 cell、局部的小view）
    val needHostViewSize: Boolean get() = viewAspectRatio > 0 || initHeightInDp > 0

    // needHostViewSize=true时启用，限定view的宽高比
    val viewAspectRatio: Float get() = 0f

    // 初始化时的高度，注意：如果同时设置了 viewAspectRatio 和 initHeightInDp
    // 则计算出来的高度不会小于 initHeightInDp
    val initHeightInDp: Int get() = 0

    // 宿主初始化高度是否跟随字号密度缩放。固定尺寸卡片可关闭，避免外层容器压缩内部固定布局。
    val scaleHostInitHeightWithFont: Boolean get() = true

    val isForceRatio: Boolean get() = viewAspectRatio > 0 && initHeightInDp == 0
}
