package com.tencent.news.core.compose.view.dialog

enum class DialogShowType {
    FullScreen,
    BottomSheet,
    Center,
    /** Snackbar 样式：从底部滑入，不阻断事件，内容完全自定义 */
    Snackbar,
    Toast,
    /** 从屏幕右侧滑入的侧边栏弹窗 */
    RightSheet
}