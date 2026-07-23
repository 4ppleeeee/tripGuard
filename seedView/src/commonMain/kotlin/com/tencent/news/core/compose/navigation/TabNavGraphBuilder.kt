package com.tencent.news.core.compose.navigation

import androidx.compose.runtime.Composable

/**
 * Tab 导航图构建器
 *
 * 提供 [composable] DSL 用于注册各 Tab 页面，
 * 风格与官方 NavGraphBuilder.composable() 保持一致。
 *
 * 使用示例：
 * ```kotlin
 * TabNavHost(navController, startDestination = "home") {
 *     composable("home") { HomePage() }
 *     composable("profile") { ProfilePage() }
 * }
 * ```
 */
class TabNavGraphBuilder internal constructor() {

    internal val destinations = mutableListOf<TabDestination>()

    /**
     * 注册一个 Tab composable 目的地
     *
     * @param route 路由标识
     * @param content 页面内容
     */
    fun composable(
        route: String,
        content: @Composable () -> Unit,
    ) {
        destinations.add(TabDestination(route, content))
    }
}

/**
 * Tab 目的地数据
 *
 * @param route 路由标识
 * @param content 页面 Composable 内容
 */
internal class TabDestination(
    val route: String,
    val content: @Composable () -> Unit,
)
