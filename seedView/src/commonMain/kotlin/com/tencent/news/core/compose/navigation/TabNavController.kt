package com.tencent.news.core.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Tab 导航控制器
 *
 * 简易版 NavController，专用于 Tab 页面切换场景。
 * 特性：
 * - 切换 Tab 时保留子页面 Compose 状态（不销毁 composable）
 * - 自动分发生命周期事件到子页面
 * - API 风格与官方 NavController 保持一致
 *
 * @param startDestination 初始选中的 Tab route
 */
@Stable
class TabNavController(
    startDestination: String,
) {
    /** 当前选中的 Tab route */
    var currentRoute: String by mutableStateOf(startDestination)
        internal set

    /** 起始目的地 */
    val startDestinationRoute: String = startDestination

    /** 已经被访问过（创建过 Composition）的 route 集合 */
    internal val visitedRoutes = mutableSetOf(startDestination)

    /**
     * 导航到指定 Tab
     *
     * @param route 目标 Tab route
     * @param builder 导航选项构建器（预留，保持与官方 API 一致的扩展性）
     */
    fun navigate(route: String, builder: TabNavOptions.() -> Unit = {}) {
        if (route == currentRoute) return
        val options = TabNavOptions().apply(builder)
        // 记录已访问的 route，用于懒加载
        visitedRoutes.add(route)
        currentRoute = route
    }

    companion object {
        /**
         * Saver 用于跨配置变更（如旋转屏幕）保存/恢复状态
         */
        fun saver(startDestination: String): Saver<TabNavController, String> = Saver(
            save = { it.currentRoute },
            restore = { savedRoute ->
                TabNavController(startDestination).also {
                    it.visitedRoutes.add(savedRoute)
                    it.currentRoute = savedRoute
                }
            },
        )
    }
}

/**
 * 导航选项（预留扩展）
 * 保持与官方 NavOptions 类似的 DSL 风格
 */
class TabNavOptions {
    var launchSingleTop: Boolean = true
}

/**
 * 创建并记忆 [TabNavController]，跨重组/配置变更保持状态
 *
 * @param startDestination 初始 Tab route
 */
@Composable
fun rememberTabNavController(
    startDestination: String,
): TabNavController {
    return rememberSaveable(saver = TabNavController.saver(startDestination)) {
        TabNavController(startDestination)
    }
}
