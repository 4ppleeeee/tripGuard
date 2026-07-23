package com.tencent.news.core.list.vm

import com.tencent.news.core.list.api.IDislikeListener
import com.tencent.news.core.util.ViewAnchor
import kotlinx.coroutines.flow.StateFlow

interface IFeedbackBtnVM {
    val showDialog: StateFlow<Boolean>      // 弹窗展示标识（无脑监听即可，内部会流转状态）
    val showDislikeAnim: StateFlow<Boolean> // 变为true时：执行负反馈删除动画

    // 展示【宿主的】负反馈弹窗（一般用于列表cell）
    fun performFeedback(viewAnchor: ViewAnchor, listener: IDislikeListener?, isRegisterDialog: Boolean = false)

    fun showDislikeDialog()     // 展示【compose的】负反馈弹窗
    fun hideDislikeDialog()     // 隐藏【compose的】负反馈弹窗

    fun performDislikeAnim()    // 执行【compose的】负反馈删除动画
}