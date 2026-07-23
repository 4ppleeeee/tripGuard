package com.tencent.news.core.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.offset
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.alpha
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.lifecycle.rememberPageLifecycleDispatcher
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow

/**
 * Tab 导航宿主
 *
 * 简易版 NavHost，专用于 Tab 页面切换场景。
 * 核心特性：
 * 1. **保留子页面状态**：所有已访问的 Tab 页面保持在 Composition 树中，切换时不会销毁
 * 2. **生命周期分发**：每个 Tab 通过 [com.tencent.news.core.compose.scaffold.lifecycle.rememberPageLifecycleDispatcher] 获取独立生命周期，
 *    内部自动完成兄弟互斥（isSelected）+ 父子联动（parentResumed）
 * 3. **懒加载**：首次访问 Tab 时才创建其 Composition
 *
 * API 风格与官方 NavHost 保持一致：
 * ```kotlin
 * TabNavHost(
 *     navController = tabNavController,
 *     startDestination = "home",
 *     modifier = modifier,
 * ) {
 *     composable("home") { HomePage() }
 *     composable("drama") { DramaPage() }
 * }
 * ```
 *
 * @param navController Tab 导航控制器
 * @param startDestination 初始 Tab route
 * @param modifier Modifier
 * @param builder 导航图 DSL 构建器
 */
@Composable
fun TabNavHost(
    navController: TabNavController,
    startDestination: String,
    modifier: Modifier = Modifier,
    builder: TabNavGraphBuilder.() -> Unit,
) {
    val graph = remember(builder) {
        TabNavGraphBuilder().apply(builder)
    }

    Box(modifier = modifier.fillMaxSize()) {
        graph.destinations.forEach { destination ->
            val isSelected = destination.route == navController.currentRoute
            val hasVisited = destination.route in navController.visitedRoutes

            // 懒加载：只有访问过的 tab 才会进入 Composition
            if (hasVisited) {
                // 使用 alpha + offset 控制显隐，保留 Composition 状态
                // 非选中页面通过 offset 移出屏幕，避免内部可滚动组件（如 HorizontalPager）
                // 在不可见时仍然拦截手势，导致外层组件无法响应滑动
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (isSelected) 1f else 0f)
                        .offset(x = if (isSelected) 0.dp else 99999.dp),
                ) {
                    // 每个 tab 通过 isSelected 获取独立生命周期：
                    // isSelected && parentResumed → ON_RESUME，否则 → ON_PAUSE
                    val childFlow = rememberPageLifecycleDispatcher(
                        isSelected = isSelected,
                        pagerName = destination.route
                    )

                    CompositionLocalProvider(
                        LocalComposePageLifecycleFlow provides childFlow,
                    ) {
                        destination.content()
                    }
                }
            }
        }
    }
}
