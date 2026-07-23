package com.tencent.news.core.pop

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.pop.PopReport.reportDismissByHigherToBeacon
import com.tencent.news.core.pop.api.IPopUpManager
import com.tencent.news.core.pop.PopReport.reportRemoveTask
import com.tencent.news.core.pop.PopReport.reportShowResultToBeacon


/**
 *   helper同时持有，task，manager，业务侧需要manager和task可以通过helper取
 */
class PopHelper(
    val popTask: KmmPopTask,
    val popManager: IPopUpManager,
) {

    private val popView: IPopUpView? by lazy { popTask.dialog }

    @OptIn(KmmInternalApi::class)
    internal fun bindPopHelper() {
        popView?.setPopHelper(this)
        popTask.popHelper = this
    }


    @OptIn(KmmInternalApi::class)
    fun dismiss() {
        removeTask()
        popView?.onDismissDialog()
    }

    @OptIn(KmmInternalApi::class)
    internal fun onOtherDialogTryShow(popTask: KmmPopTask) {
        popView?.onOtherDialogTryShow(popTask)
    }

    @OptIn(KmmInternalApi::class)
    internal fun showPopView(context: IKmmContext): Boolean {
        return popView?.onShowDialog(context) ?: false
    }

    @OptIn(KmmInternalApi::class)
    internal fun reportByNotShow() {
        popView?.reportByNotShow()
    }

    @OptIn(KmmInternalApi::class)
    internal fun reportByDismiss() {
        popView?.reportByDismiss()
    }

    @OptIn(KmmInternalApi::class)
    internal fun onPauseByItem(popTask: KmmPopTask) {
        popView?.onPauseByItem(popTask)
    }

    @OptIn(KmmInternalApi::class)
    internal fun checkBeforeRealShow(): Boolean {
        return popView?.checkBeforeRealShow() ?: false
    }

    internal fun onShowResult(result: PopResult) {
        reportShowResultToBeacon(popTask, result)
        if (result != PopResult.SUCCESS) {
            reportByNotShow()
        }
    }


    internal fun onDismissByHigher(higherTask: KmmPopTask) {
        reportDismissByHigherToBeacon(popTask, higherTask)
        reportByDismiss()
    }

    @KmmInternalApi
    fun removeTask() {
        if (popManager.removeItem(popTask)) {
            reportRemoveTask(popTask)
        }
    }
}