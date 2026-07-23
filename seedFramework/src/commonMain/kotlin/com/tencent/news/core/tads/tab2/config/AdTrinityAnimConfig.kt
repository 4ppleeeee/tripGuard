package com.tencent.news.core.tads.tab2.config

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable


@Serializable
data class AdTrinityAnimConfig(
    // 单位ms
    val pendantAnimDelay: Int = 0,          // 挂件-出现时机（行业样式才有挂件）
    val btnStage1AnimDelay: Int = 1000,     // 按钮-1阶段出现时机
    val btnStage2AnimDelay: Int = 4000,     // 按钮-2阶段出现时机
    val btnStage3AnimDelay: Int = 7000,     // 按钮-3阶段出现时机
    val miniGameStage2BtnAnimDelay: Int = 1500, // 小游戏二阶段按钮默认1.5s后变色
) : IKmmKeep