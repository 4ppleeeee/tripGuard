package com.tencent.news.core.share.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 分享渠道枚举测试
 *
 * 验证 ShareChannel 枚举定义和扩展方法的正确性，
 * 确保与外部 SDK（微信、QQ、微博、企微）的渠道映射完整。
 *
 * SDK 接入参考：微信、QQ、微博、企微官方接入文档。
 * - 微信开放 SDK: com.tencent.mm.opensdk:wechat-sdk-android-without-mta:6.6.19
 * - QQ 开放 SDK
 * - 微博 SDK: io.github.sinaweibosdk:core:12.5.0
 * - 企业微信 SDK
 */
class ShareChannelTest {

    // ==================== WeChat SDK Channel Tests ====================

    @Test
    fun `WEIXIN channel should exist for WeChat session share`() {
        val channel = ShareChannel.WEIXIN
        assertEquals("WEIXIN", channel.name)
    }

    @Test
    fun `WEIXIN_MOMENTS channel should exist for WeChat moments share`() {
        val channel = ShareChannel.WEIXIN_MOMENTS
        assertEquals("WEIXIN_MOMENTS", channel.name)
    }

    @Test
    fun `isWeiXin should return true for WEIXIN`() {
        assertTrue(ShareChannel.WEIXIN.isWeiXin())
    }

    @Test
    fun `isWeiXin should return true for WEIXIN_MOMENTS`() {
        assertTrue(ShareChannel.WEIXIN_MOMENTS.isWeiXin())
    }

    @Test
    fun `isWeiXin should return false for non-WeChat channels`() {
        assertFalse(ShareChannel.QQ.isWeiXin())
        assertFalse(ShareChannel.QZONE.isWeiXin())
        assertFalse(ShareChannel.WEIBO.isWeiXin())
        assertFalse(ShareChannel.WORK_WEIXIN.isWeiXin())
        assertFalse(ShareChannel.SYSTEM.isWeiXin())
        assertFalse(ShareChannel.COPY_LINK.isWeiXin())
    }

    // ==================== QQ SDK Channel Tests ====================

    @Test
    fun `QQ channel should exist for QQ session share`() {
        val channel = ShareChannel.QQ
        assertEquals("QQ", channel.name)
    }

    @Test
    fun `QZONE channel should exist for QQ Zone share`() {
        val channel = ShareChannel.QZONE
        assertEquals("QZONE", channel.name)
    }

    // ==================== Weibo SDK Channel Tests ====================

    @Test
    fun `WEIBO channel should exist for Sina Weibo share`() {
        val channel = ShareChannel.WEIBO
        assertEquals("WEIBO", channel.name)
    }

    // ==================== Enterprise WeChat SDK Channel Tests ====================

    @Test
    fun `WORK_WEIXIN channel should exist for enterprise WeChat share`() {
        val channel = ShareChannel.WORK_WEIXIN
        assertEquals("WORK_WEIXIN", channel.name)
    }

    // ==================== All SDK Channels Completeness Tests ====================

    @Test
    fun `all four SDK platforms should have corresponding channels`() {
        val sdkChannels = listOf(
            ShareChannel.WEIXIN,         // WeChat SDK - session
            ShareChannel.WEIXIN_MOMENTS, // WeChat SDK - moments
            ShareChannel.QQ,             // QQ SDK - session
            ShareChannel.QZONE,          // QQ SDK - QZone
            ShareChannel.WEIBO,          // Weibo SDK
            ShareChannel.WORK_WEIXIN     // Enterprise WeChat SDK (wwapi)
        )
        // Ensure all SDK channels are distinct
        assertEquals(sdkChannels.size, sdkChannels.toSet().size)
    }

    @Test
    fun `ShareChannel enum should contain all expected values`() {
        val allChannels = ShareChannel.entries
        // 18 channels total as defined in ShareChannel.kt
        assertEquals(18, allChannels.size)
    }

    @Test
    fun `SDK share channels should be subset of all ShareChannel values`() {
        val sdkChannels = setOf(
            ShareChannel.WEIXIN,
            ShareChannel.WEIXIN_MOMENTS,
            ShareChannel.QQ,
            ShareChannel.QZONE,
            ShareChannel.WEIBO,
            ShareChannel.WORK_WEIXIN
        )
        val allChannels = ShareChannel.entries.toSet()
        assertTrue(allChannels.containsAll(sdkChannels))
    }

    // ==================== Non-SDK Channel Tests ====================

    @Test
    fun `non-SDK channels should exist for local operations`() {
        // These channels do not require external SDK
        val localChannels = listOf(
            ShareChannel.SYSTEM,
            ShareChannel.COPY_LINK,
            ShareChannel.SCREENSHOT,
            ShareChannel.SAVE_IMAGE,
            ShareChannel.SAVE_VIDEO,
            ShareChannel.PDF_SHARE
        )
        localChannels.forEach { channel ->
            assertFalse(channel.isWeiXin(), "${channel.name} should not be WeChat channel")
        }
    }

    @Test
    fun `poster channels should exist for poster share scenarios`() {
        val posterChannels = listOf(
            ShareChannel.MORNING_POST,
            ShareChannel.CHANNEL_POST,
            ShareChannel.EVENT_POST,
            ShareChannel.AIQA_POST,
            ShareChannel.TIMELINE_POST,
            ShareChannel.AIGC_POSTER
        )
        assertEquals(6, posterChannels.size)
        posterChannels.forEach { channel ->
            assertFalse(channel.isWeiXin(), "${channel.name} should not be WeChat channel")
        }
    }
}
