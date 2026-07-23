package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.api.IExposure
import com.tencent.news.core.tads.game.constants.GamePageType
import kotlinx.serialization.Transient


interface IGameDto : IItemDtoDoc, IExposure, IKmmKeep {

    @Transient
    val env: GameModuleEnv      // 客户端本地绑定参数集合

    val moduleId: Int
    val moduleType: Int         // 模块类型 @GameModuleType（决定展示样式）

    val oid: String
    var moduleTitle: String
    val moduleJumpUrl: String   // 模块跳转url
    val moduleTabList: List<IGameModuleTab>?    // 页卡导航数据（模块透传json解析出来的）
    val subType: Int            // 子模块类型；目前用于样式控制（例如：0-精编通栏样式(顶部) 1-精编大卡样式(非顶部)）

    val ruleId: Int             // 规则id（模块下面可能有不同的子分类，称为‘规则’，多个规则可以聚合展示成一个大模块）
    val ruleName: String        // 规则名称（例如：游戏的分类‘动作’‘策略’）
    val ruleImg: String         // 规则额外的背景图，例如游戏分类的背景

    var hideTitle: Boolean      // 运营可额外控制，隐藏标题

    val subUrl: String                           // 活动链接
    val timestamps: Int                          // 游戏上线时间
    val themeColor: IGameSpecialModelColorInfo?    // 活动色值
    val displayType: Int                         // 0-新游 1-老游
    val bgPic: String                            // 背景大图

    val editList: List<IGameEditInfo>?      // 【重要】精编信息
    val gamePicture: List<IGamePicture>?    // 【重要】轮播图素材
    val gameList: List<IGameInfo>?          // 【重要】游戏列表
    val miniGameList: List<IMiniGameInfo>?  // 【重要】小游戏列表

    val elementTotalNum: Int    // 内容总量（出于性能优化，数据一般只下发够用的个数，一些要展示总量的场景用这个值）

    /** 个人积分信息（module_type=52时返回） */
    val userIntegralInfo: IUserIntegralInfo?
        get() = null

    /** 签到模块配置（module_type=52时返回） */
    val checkInFieldJson: ICheckInFieldJson?
        get() = null

    val btnText: String
}

/**
 * 个人积分信息接口（module_type=52 时返回）
 */
interface IUserIntegralInfo : IKmmKeep {
    val avatar: String  // 用户头像URL
    val nickname: String  // 用户昵称
    val integralNum: Int  // 积分总数
    val todaySignin: Boolean  // 今天是否已签到
}

/**
 * 签到模块 field_json 配置接口（module_type=52 时返回）
 */
interface ICheckInFieldJson : IKmmKeep {
    val activity_label: String  // "我参与的活动" 按钮文案
    val activity_url: String  // "我参与的活动" 跳转链接
    val integral_label: String  // "积分兑好礼" 按钮文案
    val integral_url: String  // "积分兑好礼" 跳转链接
    val integral_icon_url: String  // "积分兑好礼" 图标URL
}

data class GameModuleEnv(
    var isFirstModule: Boolean = false,             // 是否是列表里，第一个该类型的模块
    var pageType: GamePageType = GamePageType.NONE, // 页面类型（上报用）
    var pageOrigin: String = "",                     // 页面来源（上报用）
    var index: Int = -1,                             // 在列表的有效位置，-1 表示未设置
)
