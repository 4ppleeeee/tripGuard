package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.pop.IPopUpView
import com.tencent.news.core.pop.IPopVM
import com.tencent.news.core.pop.PopType
import com.tencent.news.core.pop.api.IPopUpManager

interface IAppPopBridge {
    /**
     * Compose弹窗显示或隐藏的回调，目前需要处理和MiniBar的冲突
     */
    fun onPopVisibilityChanged(visible: Boolean)

    @KmmInternalApi
    fun createDialog(param: DialogParam): IPopUpView?

    /**
     * Debug环境下的弹窗日志回调，用于调试弹窗出不来的问题
     * @param level 日志级别：INFO, WARN, ERROR
     * @param tag 日志标签
     * @param message 日志消息
     * @param popTaskInfo 弹窗任务信息（可选）
     */
    fun onPopDebugLog(level: String, tag: String, message: String, popTaskInfo: String? = null)
}

data class DialogParam(
    val context: IKmmContext,
    val type: PopType,
    val vm: IPopVM
)

@OptIn(KmmInternalApi::class)
fun appPopBridge(): IAppPopBridge? = QnFrameworkLogic.popBridge