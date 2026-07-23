package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic
import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenInfoApiCompatibilityTest {

    @Test
    fun appScreenInfoUsesInjectedScreenInfo() {
        val originScreenInfo = QnPlatformLogic.screenInfo
        QnPlatformLogic.screenInfo = RecordingScreenInfo(screenWidth = 750)
        try {
            assertEquals(750, appScreenInfo().getScreenWidth())
        } finally {
            QnPlatformLogic.screenInfo = originScreenInfo
        }
    }
}

private data class RecordingScreenInfo(
    private val screenWidth: Int = 0,
    private val screenHeight: Int = 0,
    private val screenWidthInch: Float = 0F,
    private val screenHeightInch: Float = 0F,
    private val dpi: Int = 0,
) : IScreenInfo {
    override fun getScreenWidth(): Int = screenWidth
    override fun getScreenHeight(): Int = screenHeight
    override fun getScreenWidthInch(): Float = screenWidthInch
    override fun getScreenHeightInch(): Float = screenHeightInch
    override fun getDpi(): Int = dpi
}
