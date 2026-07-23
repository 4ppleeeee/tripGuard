package com.tencent.news.core.pop

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.compose.platform.IComposePageArgs

/**
 * 由业务侧实现弹窗的抽象，绝大多数方法都是kmm侧调用，业务侧不许调用
 */
interface IPopUpView {
    /**
     * 要展示的时候回调，业务侧在此处理展示逻辑，弹窗上屏
     */
    @KmmInternalApi
    fun onShowDialog(context: IKmmContext): Boolean

    /**
     * 当有弹窗尝试展示时，给其他正在展示的弹窗一个回调
     */
    @KmmInternalApi
    fun onOtherDialogTryShow(popTask: KmmPopTask) {
    }

    /**
     * 弹窗消失，业务侧不允许直接调用，调用消失可使用getHelper().dismiss()
     */
    @KmmInternalApi
    fun onDismissDialog()

    /**
     * 弹窗是否正在展示
     */
    fun isDialogShowing(): Boolean = true

    /**
     * 展示前拦截，可加入业务侧的一些频控判断
     */
    @KmmInternalApi
    fun checkBeforeRealShow(): Boolean = true

    /**
     * 业务侧需要保存此helper，后续通过此helper可以操作manager和task
     */
    @KmmInternalApi
    fun setPopHelper(popHelper: PopHelper)

    fun getPopHelper(): PopHelper? = null

    /**
     * 展示被拦截上报，业务侧需要时可以实现
     */
    @KmmInternalApi
    fun reportByNotShow() {
    }

    /**
     * 被高优先级弹窗顶掉上报上报，业务侧需要时可以实现
     */
    @KmmInternalApi
    fun reportByDismiss() {
    }

    /**
     * 不需要被顶掉时，给业务侧一个回调，业务侧要是有骚操作可以在此处处理
     * @param item
     */
    @KmmInternalApi
    fun onPauseByItem(item: KmmPopTask?) {
    }
}

interface IPopVM

interface IComposePopVM : IPopVM, IComposePageArgs

object PopUpViewLocation {
    const val BOTTOM = 1
    const val BOTTOM_RIGHT = 1 shl 1
    const val FULL = 1 shl 2
}
