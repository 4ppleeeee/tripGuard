package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
internal class ListVideoState() : IListVideoState {
    private val muteFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    override val isMute: StateFlow<Boolean> = muteFlow.asStateFlow()

    override var playingVid: String? = null

    override fun changeMute(isMute: Boolean) {
        muteFlow.value = isMute
    }
}