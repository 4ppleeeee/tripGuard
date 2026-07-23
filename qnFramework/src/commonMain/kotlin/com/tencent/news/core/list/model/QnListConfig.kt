@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.list.model

sealed class QnListConfig

// 常规列表：
data class NormalListConfig constructor(
    val topPadding: Float = 0f,   // 列表顶部间距，用于避开固定的顶部组件（如ChannelBar）
    val bouncesEnable: Boolean = false  // 是否启用列表回弹（不依赖下拉刷新，独立控制）
) : QnListConfig()

// 网格列表：
data class GridListConfig constructor(
    val gridSpanSize: Int,
    val gridSpanConfig: Map<Int, Int>? = null,
) : QnListConfig()

// 瀑布流：
data class StaggeredGridListConfig constructor(
    val gridSpanSize: Int,
    val gridSpanConfig: Map<Int, Int>? = null,
    val horizontalSpacing: Float = 0f,  // 列间水平间距 (dp)
    val verticalSpacing: Float = 0f,    // item间垂直间距 (dp)
) : QnListConfig() {

    companion object {
        // 常规单列
        fun singleColumn() = StaggeredGridListConfig(
            gridSpanSize = 1
        )

        // 双列（picShowType 可配置哪些cell宽度只占1列）
        fun twoColumns(
            vararg picShowType: Int,
            horizontalSpacing: Float = 0f,
            verticalSpacing: Float = 0f
        ) = StaggeredGridListConfig(
            gridSpanSize = 2,
            gridSpanConfig = picShowType.associateWith { 1 },
            horizontalSpacing = horizontalSpacing,
            verticalSpacing = verticalSpacing
        )

        // 三列（大屏适配，picShowType 可配置哪些cell宽度只占1列）
        fun threeColumns(
            vararg picShowType: Int,
            horizontalSpacing: Float = 0f,
            verticalSpacing: Float = 0f
        ) = StaggeredGridListConfig(
            gridSpanSize = 3,
            gridSpanConfig = picShowType.associateWith { 1 },
            horizontalSpacing = horizontalSpacing,
            verticalSpacing = verticalSpacing
        )

        // 四列（超大屏适配，picShowType 可配置哪些cell宽度只占1列）
        fun fourColumns(
            vararg picShowType: Int,
            horizontalSpacing: Float = 0f,
            verticalSpacing: Float = 0f
        ) = StaggeredGridListConfig(
            gridSpanSize = 4,
            gridSpanConfig = picShowType.associateWith { 1 },
            horizontalSpacing = horizontalSpacing,
            verticalSpacing = verticalSpacing
        )
    }

}

// 垂直分页（用于音频电台等场景）：
data class VerticalPagerListConfig constructor(
    val initialPage: Int = 0,
    val beyondViewportPageCount: Int = 1,
    val userScrollEnabled: Boolean = true,
    val topPadding: Float = 0f,
    val onPageChanged: ((Int) -> Unit)? = null  // 页面切换回调，参数为当前页面索引
) : QnListConfig() {

    companion object {
        // 标准音频电台垂直分页配置
        // topPadding: 顶部间距，用于避开ChannelBar等固定元素
        fun standardAudioRadio(topPadding: Float = 0f) = VerticalPagerListConfig(
            initialPage = 0,
            beyondViewportPageCount = 1,
            userScrollEnabled = true,
            topPadding = topPadding
        )
    }
}