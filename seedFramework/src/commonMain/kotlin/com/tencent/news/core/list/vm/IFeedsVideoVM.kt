package com.tencent.news.core.list.vm

import kotlinx.coroutines.flow.MutableStateFlow

interface IFeedsVideoVM {
    val vid: String
    val playCount: Long?
    val duration: String
    val isPlaying: MutableStateFlow<Boolean>
    val canAutoPlay: MutableStateFlow<Boolean>     // 当前所在位置是否需要自动播放
    val canAutoStop: MutableStateFlow<Boolean>     // 当前所在位置是否需要自动停止
}