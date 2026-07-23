package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep


// 导航模块信息

interface IGameModuleTab : IKmmKeep {

    val tabId: String               // 导航id，例如：selected
    val tabName: String             // 导航名称，例如：精编
    val tabJumpUrl: String          // 跳转url，例如：https:\/\/n.ssp.qq.com\/choiceness
    val tabIconUrl: String          // 默认态图标
    val tabIconSelectedUrl: String  // 选中态高亮图标
    val tabNightIconUrl: String     // 夜间图标
    val tabNightIconSelectedUrl: String     // 夜间选中态高亮图标
}