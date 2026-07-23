package com.tencent.news.core.tads.oneshot.order

import com.tencent.news.core.channel.constants.NewsChannel
import com.tencent.news.core.tads.constants.AdLoid
import com.tencent.news.core.tads.constants.INVALID_NUM

/**
 * OneShot 广告场景类型
 */
enum class AdOneShotScene(
    val adChannel: String,  // 广告频道
    val insertSeq: Int,     // 广告插入到业务数据列表中的位置（从 1 开始）
    val loid: Int,          // 广告位 ID
    val adIndex: Int = 1,   // 广告相对位置
    val disableAnimation: Boolean = false // 是否做动画
) {
    /**
     * 信息流场景（要闻频道）
     * - 广告频道：news_news_top
     * - 插入位置：-1 通常信息流的策略就是替换
     * - 广告位：STREAM
     */
    FEEDS(NewsChannel.NEWS_TOP, INVALID_NUM, AdLoid.STREAM, 1),

    /**
     * 微信插件落地页场景
     * - 广告频道：news_news_top（暂时与信息流一致，后续可配置）
     * - 插入位置：2（强插位置2）
     * - 广告位：VIDEO_STREAM
     */
    WECHAT_PLUGIN_LANDING(NewsChannel.NEWS_TOP, 2, AdLoid.VIDEO_STREAM, 1),


    /**
     * 微信插件拉起要闻，由于和业务侧锚点冲突，不做动画
     * - 广告频道：news_news_top
     * - 插入位置：-1 通常信息流的策略就是替换
     * - 广告位：STREAM
     * - 是否禁止动画：true
     */
    WECHAT_PLUGIN_LANDING_NEWS(NewsChannel.NEWS_TOP, INVALID_NUM, AdLoid.STREAM, 1, true)
}
