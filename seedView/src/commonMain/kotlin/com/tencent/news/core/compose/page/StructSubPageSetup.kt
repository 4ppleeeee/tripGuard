package com.tencent.news.core.compose.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.compose.scaffold.StructPageViewModelEx.getAllFeedsList
import com.tencent.news.core.list.api.FeedsRefreshRequest
import com.tencent.news.core.list.constants.ListRefreshForward
import com.tencent.news.core.platform.api.NetState
import com.tencent.news.core.platform.api.NetStateChangeListener
import com.tencent.news.core.platform.api.appNetwork

/**
 * 子页面网络状态监听
 *
 * 统一封装 StructChannelList 和 StructSubPageView 中相同的网络监听逻辑：
 * 1. 监听网络状态变化：无网→有网时自动触发 RESET 刷新
 * 2. 页面销毁时自动清理监听器
 *
 * 注意：不在此处做初始无网检查。iOS 冷启动时网络状态模块尚未初始化完成，
 * 同步读取 netState() 可能误返回 INAVAILABLE，导致页面直接显示错误 UI。
 * 无网场景由网络请求真正失败后，经 onProcessError 自然流转到 Error 状态。
 */
@Composable
fun SubPageNetworkEffect(viewModel: IStructPageViewModel) {
    DisposableEffect(viewModel) {
        // 监听网络状态：无网→有网时自动触发刷新
        val netListener = object : NetStateChangeListener {
            override fun netStateChanged(old: NetState, new: NetState) {
                if (new != NetState.INAVAILABLE && old == NetState.INAVAILABLE) {
                    // 网络恢复，触发刷新（仅在没数据时候）
                    if (viewModel.getAllFeedsList().isEmpty()) {
                        viewModel.refresh(FeedsRefreshRequest(ListRefreshForward.RESET))
                    }
                }
            }
        }
        appNetwork().addNetStatusChangeListener(netListener)

        onDispose {
            appNetwork().removeNetStatusChangeListener(netListener)
        }
    }
}
