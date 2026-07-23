package com.tencent.news.core.share.api


/**
 * 分享渠道
 *
 * Author: joejhzhou
 * Date: 2024/10/17
 **/

enum class ShareChannel {
    /**
     * 系统分享
     */
    SYSTEM,
    /**
     * 微信
     */
    WEIXIN,

    /**
     * 朋友圈
     */
    WEIXIN_MOMENTS,

    /**
     * 手Q
     */
    QQ,

    /**
     * QQ空间
     */
    QZONE,

    /**
     * 新浪微博
     */
    WEIBO,

    /**
     * 企业微信
     */
    WORK_WEIXIN,

    /**
     * 早晚报海报分享
     */
    MORNING_POST,

    /**
     * 频道分享
     */
    CHANNEL_POST,

    /**
     * 专题事件海报分享
     */
    EVENT_POST,

    /**
     * AI QA 海报分享
     */
    AIQA_POST,
    /**
     * 脉络 海报分享
     */
    TIMELINE_POST,

    /**
     * 复制链接
     */
    COPY_LINK,

    /**
     * 截屏分享
     */
    SCREENSHOT,

    /**
     * 保存图片
     */
    SAVE_IMAGE,

    /**
     * 保存视频
     */
    SAVE_VIDEO,

    /**
     * PDF分享
     */
    PDF_SHARE,

    /**
     * AIGC海报分享
     */
    AIGC_POSTER
}

/**
 * 是否微信分享
 */
fun ShareChannel.isWeiXin() = this == ShareChannel.WEIXIN || this == ShareChannel.WEIXIN_MOMENTS