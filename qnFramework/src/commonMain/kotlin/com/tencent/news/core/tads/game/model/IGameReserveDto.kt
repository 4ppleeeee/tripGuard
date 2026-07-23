package com.tencent.news.core.tads.game.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tads.model.IAppChannelInfo


// 游戏预约信息

interface IGameReserveInfo : IKmmKeep {
    val calendarOpen: Boolean                           // 是否展示日历选项
    val calendarInfo: IGameReserveCalendarInfo?         // 预约日历信息
    val downloadOpen: Boolean                           // 是否展示wifi自动下载选项
    val autoDownloadInfo: IGameReserveAutoDownloadInfo? // 下载信息
    val phoneNumber: String                             // 后台下发的加密手机号
    val isAutoReserveInDetailPage: Boolean              // 在底层页是否自动预约
    val joinGroupUrl: String?                           // 一键加群链接
    val pushOpen: Boolean                               // 是否展示“上线后APP提醒”
    var pushEnable: Boolean                             // “上线后APP提醒”是否开启
    var phoneNumberModified: String?                    // 本地修改的明文手机号
}


// 游戏预约-下载信息

interface IGameReserveAutoDownloadInfo : IKmmKeep {

    val autoDownloadClose: Boolean      // 是否不自动勾选wifi自动下载选项
    val gameDescription: String         // 游戏介绍

    val appChannelInfo: IAppChannelInfo // 下载‘十要素’

}


interface IGameReserveCalendarInfo : IKmmKeep {
    val startTime: Long                 // 预约开始时间
    val endTime: Long                   // 预约结束时间
    val reminderTimes: List<Long>?
    val reminderMsg: String             // 预约详情介绍
    val jumpUrl: String                 // 预约落地h5
}


interface IGameReservePermissionItem : IKmmKeep {
    val permissionName: String         // 权限名
    val permissionDesc: String         // 权限描述
}


interface IGameReserveJoinGroupInfo : IKmmKeep {
    val url: String                     // 一键加群链接
}