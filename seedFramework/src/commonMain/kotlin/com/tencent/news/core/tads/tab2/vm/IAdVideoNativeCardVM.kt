package com.tencent.news.core.tads.tab2.vm

import com.tencent.news.core.tads.model.IAdWeChatStoreInfo


/**
 * 【广告】竖版视频-原生卡ViewModel接口
 */
interface IAdVideoNativeCardVM : IAdVideoCardVM, IAdVerticalVideoPageEventObserver {
    /**
     * sizeChanged回调
     */
    var onSizeChanged: AdCompanionSizeCallback?

}


/**
 * 原生卡片基础接口（所有原生卡片的公共属性）
 * 统一定义 icon + name 结构，避免重复定义
 */
interface IAdNativeBaseCardVM : IAdBigCardVM {
    val icon: IAdIconVM     // 行业图标（短剧/小游戏/直播等）
    val name: String        // 名称（短剧名/游戏名/商品名等）
}

interface IAdNativeBaseActionCardVM : IAdNativeBaseCardVM {
    val actionText: String  // 行动按钮文案
}

/**
 * 短剧小卡 ViewModel（第一阶段）
 */
interface IAdShortDramaSmallCardVM : IAdNativeBaseActionCardVM

/**
 * 短剧行动卡 ViewModel（第二三阶段）
 */
interface IAdShortDramaActionCardVM : IAdNativeBaseCardVM {
    val tags: List<String>              // 标签列表
    val actionBtn: IAdCardActionBtnVM   // 行动按钮
    val watchNumDesc: String            // 观看人数
    val labelLoopTime: Long             // 轮播时间
}

/**
 * 短剧行动卡 V2 ViewModel（第二三阶段）- 使用playletInfo数据
 */
interface IAdShortDramaActionCardV2VM : IAdNativeBaseCardVM {
    val tags: List<String>              // 标签列表（包含category和观看人数）
    val actionBtn: IAdCardActionBtnVM   // 行动按钮
    val watchNumDesc: String            // 观看人数
    val labelLoopTime: Long             // 轮播时间
}

/**
 * 旅游原生卡 - 第一阶段：固定icon + 广告主名称 + 引导文案
 */
interface IAdTravelNormalCardVM : IAdNativeBaseActionCardVM

/**
 * 旅游原生卡 - 第二三阶段：icon + 广告主名称 + 行动按钮高亮
 */
interface IAdTravelActionCardVM : IAdNativeBaseCardVM {
    val actionBtn: IAdCardActionBtnVM   // 行动按钮（固定文案：\"即可启程\"）
    val watchNumDesc: String            // 观看人数
}

/**
 * 微信小店原生卡 - 第一阶段：固定icon + "好物热销" + 引导文案
 */
interface IAdWechatStoreNormalCardVM : IAdNativeBaseActionCardVM

/**
 * 微信小店原生卡 - 第二三阶段：icon + 商品名称 + 轮播信息 + 行动按钮高亮
 */
interface IAdWechatStoreActionCardVM : IAdNativeBaseCardVM {
    val storeInfo: IAdWeChatStoreInfo?      // 微信小店信息
    val labels: List<LiveShopLabels>        // 轮播标签列表
    val actionBtn: IAdCardActionBtnVM       // 行动按钮（固定文案："去微信看看"）
    val labelLoopTime: Long                 // 轮播时间
        get() = 0L
    val defaultLabelText: String            // 默认标签文案
}

/**
 * 直播原生卡 - 第一阶段：固定icon + 直播文案
 */
interface IAdLiveStreamNormalCardVM : IAdNativeBaseActionCardVM {
    val liveShopData: IAdLiveShopDataVM     // 直播小店数据（复用）
    val liveStatusText: String              // 直播状态文案（固定："直播中"）
}

/**
 * 直播原生卡 - 第二三阶段：有商品/无商品逻辑 + 行动按钮高亮
 */
interface IAdLiveStreamActionCardVM : IAdNativeBaseCardVM {
    val liveShopData: IAdLiveShopDataVM     // 直播小店数据（复用）
    val labels: List<LiveShopLabels>        // 轮播标签（复用直播小店逻辑）
    val labelLoopTime: Long                 // 轮播时间
    val actionBtn: IAdCardActionBtnVM       // 行动按钮（支持高亮）
    val defaultLabelText: String            // 默认标签文案

    // 生命周期方法
    suspend fun onCreate()
    suspend fun onDestroy()
}

/**
 * 小游戏原生卡 - 第一阶段：固定icon + 小游戏文案
 */
interface IAdMiniGameNormalCardVM : IAdNativeBaseActionCardVM

/**
 * 小游戏原生卡 - 第二三阶段：icon + 小游戏名称 + 行动按钮高亮
 */
interface IAdMiniGameActionCardVM : IAdNativeBaseCardVM {
    val labels: List<TrinityStageMiniGameLabels>    // 轮播标签列表
    val labelLoopTime: Long                         // 轮播间隔时长
    val actionBtn: IAdCardActionBtnVM               // 行动按钮
}

/**
 * 原生卡片小卡样式，icon + 按钮变色 + 描述 结构
 */
interface IAdSmallActionCardVM : IAdNativeBaseCardVM {
    override val icon: IAdSmallCardIconVM
    val actionBtn: IAdCardActionBtnVM   // 行动按钮
    val desc: String                    // 描述

    val label: List<String>?
        get() = null
}

interface IAdIntroBigCardVM : IAdBigCardVM {
    val topCardVM: IAdSmallActionCardVM
    val introVM: IAdIntroCardVM?
    val pairVM: IAdIntroPairVM?
}

interface IAdIntroCardVM {
    val title: String
    val desc: String
}

interface IAdIntroPairVM {
    val first: IAdIntroMetricItemVM
    val second: IAdIntroMetricItemVM
    val third: IAdIntroMetricItemVM
}

interface IAdIntroMetricItemVM {
    val value: String
    val title: String
    val style: AdIntroMetricStyle
}

enum class AdIntroMetricStyle {
    TEXT,
    RATING,
    RANKING,
}

interface IAdSmallCardIconVM : IAdIconVM {
    val size: Int
    val isCenter: Boolean
}


/**
 * 原生行业卡-小说，只btn变色
 */
interface IAdNovelCardVM : IAdSmallActionCardVM

/**
 * 原生行业卡-工具，只btn变色
 */
interface IAdToolsCardVM : IAdSmallActionCardVM


interface IAdToolsBigCardVM : IAdIntroBigCardVM

/**
 * 原生行业卡-小游戏三元组信息大卡
 */
interface IAdMiniGameIntroBigCardVM : IAdIntroBigCardVM

/**
 * 原生行业卡-教育
 */
interface IAdEducationCardVM : IAdSmallActionCardVM
