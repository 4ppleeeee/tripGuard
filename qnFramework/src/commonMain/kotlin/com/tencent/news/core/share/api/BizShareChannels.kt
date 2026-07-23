package com.tencent.news.core.share.api

object BizShareChannels {
    /**
     * 海报分享渠道列表
     */
    val postShare = listOf(
        ShareChannel.SAVE_IMAGE,
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SYSTEM
    )

    /**
     * 单列表分享渠道列表 
    */
    val singleListShare = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        // 通用分享内部会检查shareImage ，如果没有，最终会过滤掉
        ShareChannel.CHANNEL_POST,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.COPY_LINK,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SYSTEM
    )

    /**
     * 早晚报分享渠道列表
     */
    val morningPost = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.MORNING_POST,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.COPY_LINK,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SCREENSHOT,
        ShareChannel.SYSTEM
    )

    /**
     * AI问答分享渠道列表
     */
    val aiQAPost = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.AIQA_POST,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.COPY_LINK,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SCREENSHOT,
        ShareChannel.SYSTEM
    )

    /**
     * 事件脉络分享渠道列表
     */
    val TimelinePost = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.TIMELINE_POST,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.COPY_LINK,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SCREENSHOT,
        ShareChannel.SYSTEM
    )

    /**
     * CP合集分享渠道列表（含截屏分享）
     */
    val cpCollectionShare = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WEIBO,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.COPY_LINK,
        ShareChannel.SCREENSHOT,
        ShareChannel.SYSTEM
    )

    /**
     * CP合集视频分享渠道列表（不含截屏分享）
     */
    val cpCollectionVideoShare = cpCollectionShare.filterNot {
        it == ShareChannel.SCREENSHOT
    }

    /**
     * 专题分享渠道列表
     */
    val eventPost = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WEIBO,
        ShareChannel.COPY_LINK,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SYSTEM
    )

    /**
     * 付费专栏详情页分享渠道列表
     * 与旧版 TencentNews 专栏页对齐：含「截屏分享」，不含「系统分享」
     */
    val columnDetail = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WEIBO,
        ShareChannel.COPY_LINK,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SCREENSHOT,
    )

    /**
     * 新闻妹对话分享渠道列表
     */
    val aigcPost = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.AIGC_POSTER,
        ShareChannel.COPY_LINK,
        ShareChannel.PDF_SHARE
    )

    /**
     * 新闻妹页面分享渠道列表
     */
    val aigcPagePost = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.COPY_LINK,
    )

    /**
     * 新闻妹页面分享渠道列表
     */
    val payGiftSendShareChannels = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.COPY_LINK,
    )


    /**
     * 音频底层页分享渠道列表
     */
    val audioPlayerShareChannels = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.COPY_LINK,
        ShareChannel.SCREENSHOT,
    )

    /**
     * 竖版视频广告分享渠道列表
     */
    val adVideoShareChannels = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.WEIBO,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.COPY_LINK,
    )

    /**
     * AIGC 视频底层页分享渠道列表
     * 首位是保存视频，其余复用 aigcPost 的渠道
     */
    val aigcVideoShare = listOf(
        ShareChannel.SAVE_VIDEO,  // 保存视频（首位）
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.COPY_LINK,
    )

    /**
     * 音频频道分享渠道列表，暂时只有鸿蒙使用
     */
    val videoChannel = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.COPY_LINK,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SYSTEM
    )

    /**
     * AIGC 表格分享渠道列表（QnMarkdownTable 专用）
     */
    val aigcTableShare = listOf(
        ShareChannel.SAVE_IMAGE,
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.WORK_WEIXIN,
        ShareChannel.SYSTEM
    )

    /**
     * 音色分享渠道列表（音色切换页"我的声音"专用）
     */
    val timbreShare = listOf(
        ShareChannel.WEIXIN,
        ShareChannel.WEIXIN_MOMENTS,
        ShareChannel.QQ,
        ShareChannel.QZONE,
        ShareChannel.WORK_WEIXIN,
    )
}
