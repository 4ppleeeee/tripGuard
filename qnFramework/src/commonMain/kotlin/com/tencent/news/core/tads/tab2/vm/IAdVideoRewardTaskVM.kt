package com.tencent.news.core.tads.tab2.vm

import kotlinx.coroutines.flow.StateFlow

interface IAdVideoRewardTaskVM : IAdVideoConfigTextVM {

    // 静态属性
    val isFinishedHasUrl: Boolean
    val onlyShowGetScore: Boolean                // 只展示获取积分按钮
    val getDurationCompare: Boolean              // 任务时长和视频时长的比较

    // 动态状态
    val getBubbleText: StateFlow<String>        // 提醒气泡文案
    val getBubbleDuration: StateFlow<Long>      // 提醒气泡展示时间
    val getBubbleTrigger: StateFlow<Long>       // 提醒气泡展示触发器，这个Long值变化后重新展示一次
    val getIconVisibility: StateFlow<Boolean>   // 获取图标是否可见状态
    val getTaskState: StateFlow<TaskUIState>    // 获取任务状态（包含进度、自动播放、状态等）

    // 功能交互
    suspend fun getShowTimesInfo(): Int         // 获取展示次数信息
    fun increaseCloseCount()                    // 增加关闭次数
    fun handleIconClick()                       // 处理图标点击事件
    fun showBubbleView()                        // 主动展示提醒气泡
    fun onVideoStart()                          // 视频开始播放时调用
    fun onVideoStop()                           // 视频暂停时调用
    fun closeIcon()                             // 关闭图标

    // 任务UI状态数据类
    data class TaskUIState(
        val progressStart: Float = 0f,
        val progressEnd: Float = 0f,
        val autoPlay: Boolean = false,
        val infinity: Boolean = false,
        val status: String = "",
        val lottieFile: String,
        val textDelegate: Map<String, String>? = null

    )
}