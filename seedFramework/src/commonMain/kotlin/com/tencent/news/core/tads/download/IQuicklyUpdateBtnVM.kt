package com.tencent.news.core.tads.download

import com.tencent.news.core.list.vm.BtnVM
import kotlinx.coroutines.flow.StateFlow

interface IQuicklyUpdateBtnVM {

    val btnVMFlow: StateFlow<BtnVM?>
    val stateFlow: StateFlow<Int>               // 下载状态

    fun onAttach()

    fun onDetach()
}