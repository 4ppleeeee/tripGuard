package com.tencent.news.core.compose.scaffold.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.kuikly.compose.ui.graphics.painter.Painter
import com.tencent.kuikly.compose.ui.unit.Dp
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.news.core.compose.scaffold.IListVideoState
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.compose.scaffold.modifiers.DtLogicParentView
import com.tencent.news.core.compose.scaffold.skin.PageSkin
import com.tencent.news.core.compose.view.ScreenshotState
import com.tencent.news.core.compose.view.bubble.BubbleViewController
import com.tencent.news.core.compose.view.dialog.DialogController
import com.tencent.news.core.compose.view.list.IQnListState
import com.tencent.news.core.extension.IStructWidgetRegistryDoc
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import kotlinx.coroutines.flow.SharedFlow

// todo 【架构说明】：品字形架构（StructComposePage）用到的相关服务接口，统一在这个文件注册
@Suppress("unused")
private object LocalPageServiceRegistry : IStructWidgetRegistryDoc // 仅用来跟踪一下文档使用

// 【最佳实践】：staticCompositionLocalOf + State
// - staticCompositionLocalOf 确保 Local 本身的变化不会触发大范围重组
// - 内部的 State<T> 让需要监听的组件可以精确订阅状态变化
// - 这样既避免了全局重组，又保持了响应式能力
private fun <T> service(defaultFactory: () -> T) = staticCompositionLocalOf(defaultFactory)

// 【品字形框架】获取Pager选中位置
val LocalStructPagePagerIndex = service<State<Int>> { mutableIntStateOf(0) }

// 【品字形框架】获取页面ViewModel（可触发页面刷新）
val LocalStructPageViewModel = service<IStructPageViewModel?> { null }

// 【品字形框架】子tab内容区距离顶部偏移量（一般是：TitleBar+ChannelBar+悬停区高度）
val LocalStructChannelOffset = service<State<Dp>> { mutableStateOf(0.dp) }

// 【品字形框架】获取页面大同虚拟父节点（可用于绑定上报）
val LocalStructDtPageRef = service<State<DtLogicParentView?>> { mutableStateOf(null) }

// 【品字形框架】列表视频自动播放状态
val LocalStructContentVideoListState = service<IListVideoState?> { null }

// 【品字形框架】页面滚动listState
val LocalStructRootListState = service<IQnListState?> { null }

// 【品字形框架】当前选中tab的listState
val LocalStructSelectedListState = service<State<IQnListState?>> { mutableStateOf(null) }

// 【专题】专题皮肤颜色
val LocalPageSkin = service<State<PageSkin?>> { mutableStateOf(null) }

// 【页面通用】监听newIntent事件（例如：scheme重复拉起页面）
val LocalComposePageNewIntentFlow = service<SharedFlow<Map<String, Any>>?> { null }

// 【页面通用】监听页面生命周期
val LocalComposePageLifecycleFlow = service<SharedFlow<PageLifecycleEvent>?> { null }

// 【页面通用】截图
val LocalScreenshot = service { ScreenshotState() }

// 【页面通用】弹窗能力
val LocalDialogController = service { DialogController() }

// 【页面通用】蓝色提示气泡能力
val LocalBubbleViewController = service { BubbleViewController() }

// 【页面通用】头部折叠状态
// （独立一个Local服务，有需要的组件局部监听，减少整体重组；header重组在鸿蒙和iOS上页面会跳动）
val LocalHeaderCollapseStatus = service<State<Boolean>> { mutableStateOf(false) }

// 【品字形框架】自定义错误页面图片Provider
val LocalErrorImagePainterProvider = service<(@Composable () -> Painter)?> { null }

// 【浮层联动】FeedsCard 点击拦截委托
// 非空时拦截 FeedsCard 默认路由，改为回调外部处理（如沉浸式视频浮层中点击切换视频）
// 默认值 null 表示不拦截，FeedsCard 走标准 appRouter 路由
val LocalFloatPageItemClickDelegate = service<((IKmmFeedsItem) -> Unit)?> { null }