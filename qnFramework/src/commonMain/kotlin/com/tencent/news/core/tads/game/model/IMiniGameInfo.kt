package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tads.model.QnAdOrder


// QZone小游戏

interface IMiniGameInfo : IKmmKeep, IGamePageTypeHolder {

    val gameId: String      // 小游戏id
    val appName: String     // 小游戏名称
    val appIcon: String     // 小游戏图标
    val appId: String       // 原始游戏id
    val appLink: String     // 小游戏链接
    val appDesc: String     // 小游戏描述
    val homeImg: String     // 小游戏首页图

    val userId: String      // 微信小游戏的id

    val labels: List<String>?   // 游戏标签
    val innerApp: Int       // 小游戏类型 @GameInnerAppType
    val platform: Int       // 小游戏平台
    val recently: Boolean   // 是否最近在玩
    val gameType: Int       // 类型:0全部，1广告，2内购
    val enableInnerOpen: Boolean // 是否端内拉起微信小游戏
    val needLogin: Boolean  // 端内拉起微信小游戏前是否需要登录

    val playingNum: Int
        get() = 0
    val displayNumber: Int
        get() = 0

    val categoryLabel: String    // 分类标签
    val highlightLabel: String   // 高亮标签
    var order: QnAdOrder? // 绑定的广告订单，用于替换小游戏曝光 点击行为
}
