package com.tencent.news.core.page.vm

import com.tencent.news.core.page.model.IStructWidgetVM
import kotlinx.coroutines.flow.StateFlow

interface IStructDialogVM : IStructWidgetVM {
    val showDialogState: StateFlow<Boolean>

    fun showDialog()
    fun dismissDialog()
}