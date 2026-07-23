package com.tencent.news.core.tads.detail.vm

import kotlinx.coroutines.flow.StateFlow

interface IAdIPLongDetailViewModel {
    val picUrl: String
    val iconText: String
    val isHideIcon: Boolean

    val canShow: StateFlow<Boolean>
    val showCloseBtn: StateFlow<Boolean>

    suspend fun onClick()
    fun onClose()
    fun onLoad()
}