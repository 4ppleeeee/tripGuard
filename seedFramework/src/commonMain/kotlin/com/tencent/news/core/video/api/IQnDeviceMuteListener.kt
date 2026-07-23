package com.tencent.news.core.video.api

interface IQnDeviceMuteManager {
    fun registerListener(listener: IQnDeviceMuteListener?)
    fun unRegisterListener(listener: IQnDeviceMuteListener?)
}

fun interface IQnDeviceMuteListener {
    fun onDeviceMuteChange(isMute: Boolean)
}