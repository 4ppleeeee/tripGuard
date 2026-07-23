package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.tads.constants.AdTrinityStage
import com.tencent.news.core.tads.constants.AdWeChatGameBulletType
import com.tencent.news.core.tads.tab2.AdIndustryResConfig
import com.tencent.news.core.tads.tab2.config.AdTrinityAnimConfig
import com.tencent.news.core.tads.tab2.config.AdVideoTemplateConfig
import com.tencent.news.core.tads.vm.IAdStoreIconVM
import kotlinx.coroutines.flow.StateFlow


// 设计稿：
// https://www.figma.com/design/wj8C0YX47o4DIkx9AP7DDU/%E8%A1%8C%E4%B8%9A%E4%B8%89%E6%AE%B5%E5%8D%A1%E7%BB%84%E4%BB%B6%E7%BB%9F%E4%B8%80?node-id=18-2218&t=cVPXbLKUUT1M06Lr-0

interface IAdVideoTrinityCardVM {

    val templateConfig: AdVideoTemplateConfig  // 模板配置

    val resConfig: AdIndustryResConfig          // 主题色、素材等配置

    val animConfig: AdTrinityAnimConfig         // 三段卡动画配置（旧的三段卡在用）

    fun getActionBtnText(stage: AdTrinityStage): String = ""

}

data class TrinityStageMiniGameLabels(
    val type: Int = AdWeChatGameBulletType.BULLET_GAME_TYPE_COMMEN,
    val content: String = "",
    val data: List<String> = emptyList(),
)

data class LiveShopLabels(
    val isGuarantee: Boolean = false,
    val content: String = "",
    val stateContent: StateFlow<String>? = null,
    val data: List<String> = emptyList(),
    val isLabelFont: Boolean = false,
    val iconFont: IconFont? = null,  // 支持图标字体
    val isSales: Boolean = false,  // 是否是销量数据（销量数据使用黄色）
    val storeIconVM: IAdStoreIconVM? = null  // 店铺标识VM（用于AdStoreIconFont组件）
)
