package com.tencent.news.core.tads.download

import com.tencent.news.core.list.vm.ClickAction
import kotlinx.coroutines.flow.StateFlow

interface IDownloadBtnVM {
    val actionTextState: StateFlow<String>      // 按钮文案
    val downloadPercentState: StateFlow<Float>  // 下载进度（取值范围 [0.0, 1.0]）
    val stateFlow: StateFlow<Int>               // 下载状态

    fun getClickAction(): ClickAction

    fun onCreate()
    fun onDestroy()
    fun onRefresh()

    fun isDownloadingStyle(state: Int): Boolean         // 是否支持下载样式
}
