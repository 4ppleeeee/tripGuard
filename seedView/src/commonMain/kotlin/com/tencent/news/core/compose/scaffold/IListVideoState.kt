package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.StateFlow

@Stable
interface IListVideoState {
    val isMute: StateFlow<Boolean>
    var playingVid: String?

    fun changeMute(isMute: Boolean)
}