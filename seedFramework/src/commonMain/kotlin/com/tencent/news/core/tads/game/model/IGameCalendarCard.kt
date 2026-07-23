package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep

interface IGameCalendarCard : IKmmKeep {
    val calendarCardEnabled: Boolean           // 日历卡片开关
    val calendarCardInfo: IGameCalendarCardInfo?  // 日历卡片信息（单个）
}

/**
 * 游戏日历卡片单项信息接口
 */
interface IGameCalendarCardInfo : IKmmKeep {
    val date: String      // 日期，格式：2026-02-26
    val label: String     // 标签，如：版本更新、限时活动、新赛季
    val urlType: Int      // 跳转类型：0=默认逻辑，1=使用url直跳
    val url: String       // 跳转链接
}
