package com.tencent.news.core.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.tencent.kuikly.compose.foundation.layout.PaddingValues
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.pager.HorizontalPager
import com.tencent.kuikly.compose.foundation.pager.PagerDefaults
import com.tencent.kuikly.compose.foundation.pager.PagerState
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.lifecycle.rememberPageLifecycleDispatcher
import com.tencent.news.core.compose.scaffold.registry.LocalComposePageLifecycleFlow

/**
 * 带生命周期管理的 HorizontalPager
 *
 * 在 [HorizontalPager] 基础上，为每个 page 自动注入独立的页面生命周期流：
 * - 当前选中的 page 收到 ON_RESUME，非选中的 page 收到 ON_PAUSE
 * - 父页面 pause 时，当前选中的 page 也会同步 pause（父子联动）
 * - 去重保护：同一 page 不会连续收到相同的 RESUME / PAUSE
 *
 * 子组件可通过 [LocalComposePageLifecycleFlow] 或 CollectPageOnResume / CollectPageOnPause 消费事件。
 *
 * 使用方式与 [HorizontalPager] 完全一致：
 * ```kotlin
 * QnHorizontalPager(state = pagerState) { pageIndex ->
 *     // 这里的组件自动拥有独立的生命周期
 *     MyPageContent(pageIndex)
 * }
 * ```
 */
@Composable
fun QnHorizontalPager(
    state: PagerState,
    modifier: Modifier = Modifier.fillMaxSize(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    beyondViewportPageCount: Int = PagerDefaults.BeyondViewportPageCount,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    userScrollEnabled: Boolean = true,
    key: ((index: Int) -> Any)? = null,
    pagerName: ((index: Int) -> String?)? = null,
    pageContent: @Composable (page: Int) -> Unit,
) {
    HorizontalPager(
        state = state,
        modifier = modifier,
        contentPadding = contentPadding,
        beyondViewportPageCount = beyondViewportPageCount,
        verticalAlignment = verticalAlignment,
        userScrollEnabled = userScrollEnabled,
        key = key,
    ) { pageIndex ->
        val childFlow = rememberPageLifecycleDispatcher(
            isSelected = state.currentPage == pageIndex,
            pagerName = pagerName?.invoke(pageIndex)
        )

        CompositionLocalProvider(
            LocalComposePageLifecycleFlow provides childFlow
        ) {
            pageContent(pageIndex)
        }
    }
}
