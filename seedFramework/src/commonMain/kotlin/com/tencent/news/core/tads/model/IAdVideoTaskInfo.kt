package com.tencent.news.core.tads.model

interface IAdVideoTaskInfo {
    // 是否展示任务
    val taskDisplay: Boolean

    // 任务时长
    val taskDuration: Int

    // 任务积分
    val taskPoints: Int
}