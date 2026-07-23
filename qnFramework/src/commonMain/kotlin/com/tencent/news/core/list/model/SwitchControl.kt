package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.serializer.SafeInt
import kotlinx.serialization.Serializable

interface ISwitchControl : IKmmKeep {
    val listenCountDisplayDisable: SafeInt  // 听播数展示禁用
    val enableLuckyBag: Int                // 福袋是否开启
    val autoPlayDisabled: Int               // 自动播放是否禁用
    val playFlag: Int                       // 播放标志
    val safeControl: Int                    // 安全管控标志 -> 沉浸式视频不能出 跳TAB2按钮 的开关
    val politicalOption: Int                // 是否是领导人模板，1-是，0-不是
    val enableAiShare: Int                       // 分享菜单是否显示新闻妹
}

@Suppress("ConstructorParameterNaming", "VariableNaming")
@Serializable
class SwitchControl : BaseKmmModel(), ISwitchControl, IKmmKeep {

    private var listen_count_display_disable: SafeInt = 0
    private var lucky_bag_enabled: Int = 0
    private var auto_play_disabled: Int = 0
    private var play_flag: Int = 0
    private var safe_control: Int = 0
    private var political_option: Int = 0
    private var ai_switch: Int = 0

    override val listenCountDisplayDisable: SafeInt
        get() = listen_count_display_disable
    override val enableLuckyBag: Int
        get() = lucky_bag_enabled
    override val autoPlayDisabled: Int
        get() = auto_play_disabled
    override val playFlag: Int
        get() = play_flag
    override val safeControl: Int
        get() = safe_control
    override val politicalOption: Int
        get() = political_option
    override val enableAiShare: Int
        get() = ai_switch
}