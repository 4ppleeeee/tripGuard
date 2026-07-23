package com.tencent.news.core.tads.feeds.vm

import kotlinx.coroutines.flow.StateFlow

// 时间线主题枚举
enum class AdTimelineTheme {
    BLUE, GOLDEN, GREEN, ORANGE, GREY, DEFAULT
}

// 时间线行动按钮图标类型
enum class AdTimelineActionIconType {
    RIGHT_ARROW,  // 普通类型：右箭头
    CHECK         // 预约类型：对勾（已预约）
}

/**
 * 时间线挂件区域 VM（倒计时 + Logo + 文字）
 */
interface IAdTimelineAnchorVM {
    val anchorText: StateFlow<String?>                  // 挂件文字（如"距离福利直播"/"剩余时间"）；倒计时结束自动清空
    val anchorImageUrl: String?                         // 挂件中间 Logo URL
    val showAnchorBg: StateFlow<Boolean>                // 是否展示挂件背景（随文案/倒计时变化：都为空时隐藏）
    val hasLogo: Boolean

    // 倒计时状态
    val countdownDays: StateFlow<Int>                    // 天数（>=1 时展示）
    val countdownTimeText: StateFlow<String>             // 格式化后的时间文本 "HH:MM:SS"
    val isCountdownVisible: StateFlow<Boolean>           // 倒计时是否可见

    fun onAttach()                                      // View attach 时启动倒计时
    fun onDetach()                                      // View detach 时暂停倒计时
}

/**
 * 时间线主框架 VM（包含挂件 + 品牌图 + 行动按钮 + 渐变背景）
 */
interface IAdTimelineMainFrameVM {
    val theme: AdTimelineTheme                  // 主题色
    val brandImageUrl: String?                  // 左上品牌图 URL（日间）
    val brandImageNightUrl: String?             // 左上品牌图 URL（夜间）
    val buttonText: String                      // 行动按钮文案

    val actionBtnIcon: StateFlow<AdTimelineActionIconType>  // 行动按钮图标（预约时为对勾，普通时为右箭头）

    val anchorVM: IAdTimelineAnchorVM?          // 挂件区域 VM（数据不合法时为 null）

    fun onActionClick()                         // 行动按钮点击
}
