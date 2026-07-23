package com.tencent.news.core.share.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 分享数据模型测试
 *
 * 验证 IKmmShareDataOption 和 KmmShareDataOptionBuilder 的数据构建正确性，
 * 确保分享数据能正确传递给各 SDK 平台。
 *
 * SDK 接入参考：微信、QQ、微博、企微官方接入文档。
 * - 微信分享需要 imageWeiXinQqUrls（缩略图限制 32KB/128KB）
 * - QQ 分享需要 imageWeiXinQqUrls
 * - 微博分享需要 imageWeiBoQZoneUrls
 * - 企微分享复用微信的分享参数
 */
class ShareDataModelTest {

    // ==================== KmmShareDataOptionBuilder Default Values Tests ====================

    @Test
    fun `KmmShareDataOptionBuilder should have correct default values`() {
        val option = KmmShareDataOptionBuilder()

        assertFalse(option.onlyFirstLine)
        assertFalse(option.isUnSafeComment)
        assertFalse(option.useShareInfoFirst)
        assertNull(option.imageWeiXinQqUrls)
        assertNull(option.imageWeiBoQZoneUrls)
        assertEquals("", option.sharePos)
        assertFalse(option.showPosterPreview)
        assertEquals(ShareSceneType.DEFAULT, option.shareScene)
        assertNull(option.aiMetadata)
        assertNull(option.posterShareChannel)
        assertEquals("", option.videoUrl)
    }

    // ==================== WeChat SDK Data Tests ====================

    @Test
    fun `imageWeiXinQqUrls should be settable for WeChat share`() {
        val option = KmmShareDataOptionBuilder()
        val urls = arrayOf<String?>("https://example.com/thumb.jpg")
        option.imageWeiXinQqUrls = urls

        assertEquals(1, option.imageWeiXinQqUrls?.size)
        assertEquals("https://example.com/thumb.jpg", option.imageWeiXinQqUrls?.get(0))
    }

    @Test
    fun `imageWeiXinQqUrls should support null values for WeChat share fallback`() {
        val option = KmmShareDataOptionBuilder()
        val urls = arrayOf<String?>(null)
        option.imageWeiXinQqUrls = urls

        assertEquals(1, option.imageWeiXinQqUrls?.size)
        assertNull(option.imageWeiXinQqUrls?.get(0))
    }

    @Test
    fun `imageWeiXinQqUrls should support multiple images`() {
        val option = KmmShareDataOptionBuilder()
        val urls = arrayOf<String?>("https://example.com/img1.jpg", "https://example.com/img2.jpg")
        option.imageWeiXinQqUrls = urls

        assertEquals(2, option.imageWeiXinQqUrls?.size)
    }

    // ==================== QQ SDK Data Tests ====================

    @Test
    fun `QQ share should use same imageWeiXinQqUrls as WeChat`() {
        // QQ and WeChat share the same thumbnail URL field
        val option = KmmShareDataOptionBuilder()
        val urls = arrayOf<String?>("https://example.com/qq_thumb.jpg")
        option.imageWeiXinQqUrls = urls

        // QQ SDK uses the same image URLs as WeChat SDK
        assertEquals("https://example.com/qq_thumb.jpg", option.imageWeiXinQqUrls?.get(0))
    }

    // ==================== Weibo SDK Data Tests ====================

    @Test
    fun `imageWeiBoQZoneUrls should be settable for Weibo share`() {
        val option = KmmShareDataOptionBuilder()
        val urls = arrayOf<String?>("https://example.com/weibo_img.jpg")
        option.imageWeiBoQZoneUrls = urls

        assertEquals(1, option.imageWeiBoQZoneUrls?.size)
        assertEquals("https://example.com/weibo_img.jpg", option.imageWeiBoQZoneUrls?.get(0))
    }

    @Test
    fun `imageWeiBoQZoneUrls should support null values for Weibo share fallback`() {
        val option = KmmShareDataOptionBuilder()
        val urls = arrayOf<String?>(null)
        option.imageWeiBoQZoneUrls = urls

        assertEquals(1, option.imageWeiBoQZoneUrls?.size)
        assertNull(option.imageWeiBoQZoneUrls?.get(0))
    }

    @Test
    fun `Weibo and QZone should use separate image URLs from WeChat and QQ`() {
        val option = KmmShareDataOptionBuilder()
        val wxQqUrls = arrayOf<String?>("https://example.com/wx_thumb.jpg")
        val weiboQzoneUrls = arrayOf<String?>("https://example.com/weibo_img.jpg")
        option.imageWeiXinQqUrls = wxQqUrls
        option.imageWeiBoQZoneUrls = weiboQzoneUrls

        // WeChat/QQ and Weibo/QZone use different image URL fields
        assertEquals("https://example.com/wx_thumb.jpg", option.imageWeiXinQqUrls?.get(0))
        assertEquals("https://example.com/weibo_img.jpg", option.imageWeiBoQZoneUrls?.get(0))
    }

    // ==================== Share Scene Tests ====================

    @Test
    fun `ShareSceneType should have all expected values`() {
        val allScenes = ShareSceneType.entries
        assertEquals(4, allScenes.size)
        assertTrue(allScenes.contains(ShareSceneType.DEFAULT))
        assertTrue(allScenes.contains(ShareSceneType.MORNING_POST))
        assertTrue(allScenes.contains(ShareSceneType.CHANNEL_SHARE))
        assertTrue(allScenes.contains(ShareSceneType.AIGC_POSTER))
    }

    @Test
    fun `shareScene should be settable`() {
        val option = KmmShareDataOptionBuilder()
        option.shareScene = ShareSceneType.MORNING_POST

        assertEquals(ShareSceneType.MORNING_POST, option.shareScene)
    }

    @Test
    fun `showPosterPreview should be settable for morning post scene`() {
        val option = KmmShareDataOptionBuilder()
        option.showPosterPreview = true
        option.shareScene = ShareSceneType.MORNING_POST

        assertTrue(option.showPosterPreview)
        assertEquals(ShareSceneType.MORNING_POST, option.shareScene)
    }

    // ==================== Video URL Tests ====================

    @Test
    fun `videoUrl should be settable for save video operation`() {
        val option = KmmShareDataOptionBuilder()
        option.videoUrl = "https://example.com/video.mp4"

        assertEquals("https://example.com/video.mp4", option.videoUrl)
    }

    @Test
    fun `videoUrl should default to empty string`() {
        val option = KmmShareDataOptionBuilder()
        assertEquals("", option.videoUrl)
    }

    // ==================== Share Position Tests ====================

    @Test
    fun `sharePos should be settable for tracking`() {
        val option = KmmShareDataOptionBuilder()
        option.sharePos = "feed_detail"

        assertEquals("feed_detail", option.sharePos)
    }

    // ==================== useShareInfoFirst Tests ====================

    @Test
    fun `useShareInfoFirst should be settable`() {
        val option = KmmShareDataOptionBuilder()
        option.useShareInfoFirst = true

        assertTrue(option.useShareInfoFirst)
    }

    // ==================== isUnSafeComment Tests ====================

    @Test
    fun `isUnSafeComment should be settable`() {
        val option = KmmShareDataOptionBuilder()
        option.isUnSafeComment = true

        assertTrue(option.isUnSafeComment)
    }
}
