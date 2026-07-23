package com.tencent.news.core.tads.feeds.vm

import com.tencent.news.core.list.vm.IClickVM
import com.tencent.news.core.tads.constants.AdGdtClickActType

// 点击 VM
interface IAdReportVM {

    fun getClickVM(actType: AdGdtClickActType = AdGdtClickActType.DEFAULT_CLICK): IClickVM?

    fun onExpose()

}