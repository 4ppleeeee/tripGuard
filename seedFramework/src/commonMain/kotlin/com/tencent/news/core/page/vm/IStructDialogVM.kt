package com.tencent.news.core.page.vm

import androidx.annotation.CallSuper
import com.tencent.news.core.page.model.IStructWidgetVM
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface IStructDialogVM : IStructWidgetVM {
    val showDialogState: StateFlow<Boolean>

    var isLandscape: Boolean

    /** 标记弹窗是否正在展示。 */
    var isShowing: Boolean

    fun showDialog()
    fun dismissDialog()
}

abstract class BaseStructDialogVM : IStructDialogVM {
    override val showDialogState = MutableStateFlow(false)
    override var isLandscape = false
    override var isShowing = false

    @CallSuper
    override fun showDialog() {
        isShowing = true
        showDialogState.update { true }
    }

    @CallSuper
    override fun dismissDialog() {
        showDialogState.update { false }
    }
}
