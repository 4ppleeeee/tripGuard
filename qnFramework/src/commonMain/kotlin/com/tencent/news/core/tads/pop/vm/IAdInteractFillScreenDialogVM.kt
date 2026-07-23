package com.tencent.news.core.tads.pop.vm

import kotlinx.coroutines.flow.StateFlow

/**
 * 互动蒙层单个 item 的数据接口
 *
 * - img1: 背景图
 * - img2: 切换按钮图
 * - img3: 行动按钮图
 */
interface IAdInteractItemVM {
    val backgroundImageUrl: String   // extendMaterial.img1 — 背景图片 URL
    val switchButtonImageUrl: String // extendMaterial.img2 — 切换按钮图片 URL
    val actionButtonImageUrl: String // extendMaterial.img3 — 行动按钮图片 URL
}

/**
 * 互动蒙层 VM 接口
 *
 */
interface IAdInteractFillScreenDialogVM : IAdFillScreenDialogVM {
    val interactItems: List<IAdInteractItemVM>  // 互动轮播 item 列表
    val carouselIntervalMs: Long              // 轮播间隔（毫秒），0 表示不自动轮播

    // 互动蒙层的 content 内含可交互的切换按钮，需要渲染在通用组件层（AdClickableFrame 等）之上
    override val contentAboveCommonComponents: Boolean get() = true

    // ==================== 轮播状态 ====================

    val selectedIndex: StateFlow<Int>           // 当前选中的 item 索引
    val outgoingIndex: StateFlow<Int?>          // 正在退场的 item 索引（用于交叉渐变动画）
    val carouselStopped: StateFlow<Boolean>     // 用户手动切换后停止自动轮播

    /** 切换到指定 index，同时设置退场动画 */
    fun switchTo(targetIndex: Int) {}
    /** 退场动画结束后，由 UI 层调用清除 outgoingIndex */
    fun clearOutgoing(expectedIndex: Int) {}
    /** 用户点击切换按钮：切换 item + 停止自动轮播 + 跳过倒计时 */
    fun onSwitchButtonClick(index: Int) {}
    /** 用户点击某个 item 的行动按钮 */
    fun onItemButtonClick(index: Int) {}
    /** 用户触发跳过倒计时（如点击切换按钮后） */
    fun onTriggerSkipCountdown() {}
}
