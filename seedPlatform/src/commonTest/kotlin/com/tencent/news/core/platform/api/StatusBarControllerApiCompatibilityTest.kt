package com.tencent.news.core.platform.api

import kotlin.test.Test

class StatusBarControllerApiCompatibilityTest {

    @Test
    fun statusBarControllerOwnsVisibilityApi() {
        val controller: IStatusBarController = RecordingStatusBarController()

        controller.setStatusBarVisibility(visible = false)
        controller.setStatusBarVisibility(visible = true)
    }
}

private class RecordingStatusBarController : IStatusBarController {
    override fun setWhiteBar() = Unit
    override fun setBlackBar() = Unit
    override fun resetStatusBar() = Unit
    override fun setCustomBar(textColor: String, backgroundColor: String) = Unit
    override fun setStatusBarVisibility(visible: Boolean) = Unit
}
