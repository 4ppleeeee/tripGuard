package com.tencent.news.core.tads.tab2.config

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeToBoolean
import com.tencent.news.core.extension.safeToInt
import com.tencent.news.core.extension.safeToLong
import com.tencent.news.core.tads.constants.INVALID_NUM
import com.tencent.news.core.tads.tab2.AdIndustryResConfig
import com.tencent.news.core.tads.tab2.vm.AdBigCardColorStyle
import kotlinx.serialization.Serializable

@Serializable
data class AdVideoCardConfig(
    val cardId: String = "",        // 卡片标识id（上报 + 关联映射表用）
    val animStart: Long = 0,        // 动画启动时间（毫秒），=0代表直接展示，没有入场动画
    val stayDuration: Long = 0,     // 动画停留时长（毫秒），<=0代表不消失
    val enterDuration: Int = 400,   // 动画进场时间
    val exitDuration: Int = 400,    // 动画退场时间
    val step: String = "",                          // 所属阶段（上报用）
    val clickEnable: Boolean = true,                // false的话会屏蔽点击（点了没反应，不会透到背后）
    val closeCard: AdVideoTemplateConfig? = null,   // 关闭动画后展现的样式
    val resConfig: AdIndustryResConfig? = null,     // 资源配置（主题色等信息）

    private val extra: Map<String, String>? = null, // 特殊组件的额外配置
) : IKmmKeep {

    // 闪光动画播放时间
    val shineTime: Int get() = extra?.get("shineTime").safeToInt(INVALID_NUM)

    // 按钮高亮动画播放时间
    val changeColorTime: Long get() = extra?.get("changeColorTime").safeToLong(INVALID_NUM.toLong())

    // 小游戏轮播开关
    val miniGameLoopSwitch: Boolean get() = extra?.get("miniGameLoopSwitch").safeToBoolean(false)

    // 小游戏扫光动画-默认是播放扫光动画
    val miniGameShimmerAnimation: Boolean
        get() = extra?.get("miniGameShimmerAnimation").safeToBoolean(true)

    // 轮播动画时间间隔
    val loopTime: Long get() = extra?.get("loopTime").safeToLong(INVALID_NUM.toLong())

    // 后一张卡片数据无效时，是否保持当前卡片不退场
    val keepWhenNextCardInvalid: Boolean
        get() = extra?.get("keepWhenNextCardInvalid").safeToSwitchBoolean(false) ||
                extra?.get("keepWhenBigCardInvalid").safeToSwitchBoolean(false)

    // 客服问答轮播动画样式：1=仅文案上下轮播（默认），2=问答气泡整体切换
    val consultQuestionAnimStyle: Int get() = extra?.get("consultQuestionAnimStyle").safeToInt(1)

    // 大卡色值样式：darkStyle=true 透明黑色背景模式
    val colorStyle: AdBigCardColorStyle
        get() {
            val isDark = extra?.get("darkStyle").safeToBoolean(false)
            return if (isDark) AdBigCardColorStyle.Dark else AdBigCardColorStyle.White
        }

    // 是否展示特殊行业文案逻辑（例如：直播小店、微信小店）
    val useIndustryText: Boolean get() = extra?.get("useIndustryText").safeToBoolean(false)

    // 直购/微信小店小卡是否展开
    val animStartTime: Long get() = extra?.get("animStartTime").safeToLong(INVALID_NUM.toLong())

    fun isValid(): Boolean = cardId.isNotEmpty()
    fun notNeedAnimation(): Boolean = (animStart <= 0L && enterDuration <= 0 && exitDuration <= 0)
}

private fun String?.safeToSwitchBoolean(defaultValue: Boolean): Boolean =
    when (this) {
        "1" -> true
        "0" -> false
        else -> safeToBoolean(defaultValue)
    }
