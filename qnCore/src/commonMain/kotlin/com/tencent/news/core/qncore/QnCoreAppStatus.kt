package com.tencent.news.core.qncore

object QnCoreAppStatus {

    private var personalizedSwitchOpen: Boolean = true
    private var newsTopManualMode: Boolean = false
    private var autoPlayListVideoPolicy: (channel: String) -> Boolean = { false }

    fun isPersonalizedSwitchOpen(): Boolean = personalizedSwitchOpen

    fun setPersonalizedSwitch(enabled: Boolean) {
        personalizedSwitchOpen = enabled
    }

    fun isInNewsTopManualMode(): Boolean = newsTopManualMode

    fun setInNewsTopManualMode(enabled: Boolean) {
        newsTopManualMode = enabled
    }

    fun canAutoPlayListVideo(channel: String): Boolean = autoPlayListVideoPolicy(channel)

    fun setAutoPlayListVideoPolicy(policy: (channel: String) -> Boolean) {
        autoPlayListVideoPolicy = policy
    }
}
