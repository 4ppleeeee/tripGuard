package com.tencent.news.core.tads.constants

import com.tencent.news.core.app.constants.SchemeFrom
import com.tencent.news.core.platform.api.getShiplySwitch


/**
 * 广告位：
 *   P0  重要位置 或 频繁投放
 *   P1  相对小场景 或 偶尔有投放
 *   P2  非常小场景 或 非常少时间会用到，需要存在
 *   P3  一年中也基本没有用到的
 *   P4  已经下线或无效（可删）
 *   P5  客户端遗留代码，投放系统中已没有（可删）
 */
@Suppress("MemberVisibilityCanBePrivate")

object AdLoid {
    // 【腾讯文档】新闻客户端广告位置类型盘点（标记P0-P4优先级）
    // https://docs.qq.com/sheet/DS2pTaE5OemlYSUlk?tab=BB08J2

    // 【混排】走混排的场景ams一般一个位置会给多个广告，让混排服务再筛选一遍最优单的

    const val NONE = -1                     // 无广告

    const val SPLASH = 0                    // 【P0】闪屏
    const val STREAM = 1                    // 【P0】【major】【混排】信息流
    const val ARTICLE_PIC = 2               // 【P0】【major】图文底层页 尾部大图
    const val ARTICLE_COMMENT = 5           // 【P0】【major】图文评论页
    const val ARTICLE_REL_READING = 10      // 【P0】相关阅读
    const val VIDEO_STREAM = 11             // 【P0】【major】【混排】视频底层页 信息流
    const val LIST_BANNER = 13              // 【P0】特型banner
    const val ARTICLE_BANNER_TOP = 17       // 【P1】【major】图文底层页 栏目冠名 header广告
    const val ARTICLE_BANNER_BOTTOM = 19    // 【P1】图文底层页 栏目冠名 底部广告
    const val HOT_SELECTION = 23            // 【P1】热点精选 焦点图广告
    const val TOPIC_CONTENT = 31            // 【P1】话题底层页

    @Deprecated("已下线")
    const val DYNAMIC_CONTENT = 32          // 【P1】【major】CP动态底层文末广告
    const val VERTICAL_VIDEO = 33           // 【P0】【major】【混排】竖版视频

    // https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/20240513143515/loid38.mp4
    const val BOTTOM_FLOAT = 38             // 【P1】底浮追光banner
    const val BRAND_GIFT = 39               // 【P0】【major】品牌献礼
    const val SEARCH_LIST = 40              // 【P1】搜索页广告（hippy实现，客户端没有了）

    @Deprecated("已下线")
    const val COMPANION_DETAIL = 41         // 【P2】横版视频挂卡随播广告（视频底层页/视频专辑页）
    const val DYNAMIC_COMMENT = 43          // 【P1】动态底层页评论广告
    const val DYNAMIC_RELATED = 44          // 【P1】动态底层页推荐信息流广告
    const val VIDEO_COMMENT = 48            // 【P0】【major】视频底层页评论
    const val SELECTED_LIST = 51            // 【P1】热点精选信息流广告
    const val TOPIC_TOP = 61                // 【P1】话题置顶广告

    // https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/20240513143515/loid65.mp4
    const val BRAND_BLIND_BOX = 65          // 【P1】大牌盲盒广告

    const val REWARD = 78                   // 【P1】【major】激励广告-使用实体频道
    const val EVENT_STREAM = 79             // 【P0】【major】【混排】事件底层页广告
    const val LONG_VIDEO_BANNER = 84        // 【P1】【major】长视频底层页广告
    const val SLIDESHOW = 85                // 【P1】长视频底部-创新轮播banner
    const val TAG_COLLECTION = 90           // 【P2】tag合集底层页
    const val ARTICLE_BOTTOM_FLOAT = 93     // 【P1】文章底浮层广告
    const val ARTICLE_MID = 98              // 【P0】图文底层页文中广告
    const val EVENT_BANNER = 102            // 【P1】专题页顶部banner广告
    const val TAB2_COMPANION = 105          // 【P1】tab2竖版视频挂卡随播广告
    const val EVENT_BRAND_GIFT = 107        // 【P1】专题页弹层
    @Deprecated("早报已切compose，宿主代码已下掉，需要的时候重新做")
    const val MORNING_POST_BANNER = 110     // 【P1】早晚报招商广告 @7180
    const val NEWS_DETAIL_FEEDS = 113       // 【P1】【混排】push落地页-要闻推荐信息流 @7210

    const val REAR_LIVE = 1000              // 【P0】视频列表页后贴
    const val BOTTOM_REAR_LIVE = 1001       // 【P0】视频底层页后贴
    const val PRE_LIVE = 1002               // 【P2】视频前贴

    const val OLYMPIC_CHANNEL_LIST = 125        // 【P2】二级频道奥运挂件
    const val OLYMPIC_HORIZONTAL_VIDEO = 127    // 【P2】横版视频奥运挂件
    const val OLYMPIC_PIC_DETAIL = 126          // 【P2】图文底层奥运挂件

    const val RESERVE_CARD = 129            // 【P1】活动预约大卡
    const val VERTICAL_VIDEO_HORIZON = 130  // 【P0】【major】【混排】竖版视频-横屏
    const val PUSH_VIDEO_STREAM = 132       // 【P0】【major】【混排】push视频底层页 信息流
    const val VERTICAL_RECOMMEND = 133      // 【P0】tab2 推荐视频
    const val WEIXIN_VIDEO_STREAM = 134     // 【P0】【major】【混排】微信插件视频底层页 信息流

    const val SEARCH_BANNER = 137           // 【P1】搜索首页Banner

    // https://file.tapd.woa.com/compress/compress_img/1400/tapd_10161211_base64_1737529823_173.png?src=/tfl/captures/2025-01/tapd_10161211_base64_1737529823_173.png
    const val AD_PAID_PUSH_DETAILS = 138    // 【P0】付费广告、付费push-视频/图文的落地页

    const val ARTICLE_AI_SUMMARY_YB = 139   // 【P2】文中ai总结尾部广告
    const val MID_INSERT_SPEC_AD = 140      // 【P1】文中特殊插入广告，目前只有元宝会投放
    const val MARKET_TOP_BANNER = 141       // 【P0】营销Banner（类似loid=13）
    const val REWARD_STREAM = 143           // 【P1】激励信息流

    const val IP_VIDEO_PENDANT = 149        // 出品底层页挂件

    const val HIGHLIGHT_PENDANT = 153       // 【P1】高光挂件（实时请求品牌挂件）
    const val MINI_GAME = 157               // 【P2】小游戏中心

    // todo tips：新增广告位时，要记得在 AdLoidRegistry 添加映射关系

    const val CUSTOM_HORIZONTAL_GAME_LIST = 10000
    const val CUSTOM_GAME_HANDPICK_MODULE = 10001   // 【P0】游戏精选模块（游戏频道顶部webcell）

    const val EXTRA_ADD = 20000       // 上报场景，如果信息流和单独请求都有，单独请求传loid时加上这个，作为区分
    const val EXTRA_PREROLL = 30001   // 上报场景，前贴
    const val EXTRA_POSTROLL = 30002  // 上报场景，后贴

    // 频道信息流广告位（影响行动按钮文案逻辑）
    fun isTimelineLoid(loid: Int): Boolean = loid in setOf(
        STREAM, LIST_BANNER, MARKET_TOP_BANNER
    )

    // 所有列表类型广告位（可以插入到信息流里的广告位类型【新增广告位时，要记得补充这里】）
    fun isFeedsLoid(loid: Int): Boolean = loid in setOf(
        STREAM,
        ARTICLE_REL_READING,
        VIDEO_STREAM,
        PUSH_VIDEO_STREAM,
        WEIXIN_VIDEO_STREAM,
        VERTICAL_RECOMMEND,
        LIST_BANNER,
        MARKET_TOP_BANNER,
        VERTICAL_VIDEO,
        VERTICAL_VIDEO_HORIZON,
        SELECTED_LIST,
        EVENT_STREAM,
        LONG_VIDEO_BANNER,
        TAG_COLLECTION,
        NEWS_DETAIL_FEEDS
    )

    // cmp投放的信息流banner（这些不会回传新鲜度、不受端智能逻辑替换）
    fun isFeedsBanner(loid: Int): Boolean = loid in setOf(
        LIST_BANNER, MARKET_TOP_BANNER
    )

    // 视频落地页广告位
    fun isVideoStreamLoid(loid: Int): Boolean = loid in setOf(
        WEIXIN_VIDEO_STREAM, PUSH_VIDEO_STREAM, VIDEO_STREAM
    )

    // 重要广告位，需要上报错误监控
    fun isImportantMonitorLoid(loid: Int): Boolean = loid in setOf(
        STREAM,             // 信息流
        VERTICAL_VIDEO,     // 竖版视频
        VIDEO_STREAM, PUSH_VIDEO_STREAM, WEIXIN_VIDEO_STREAM,   // 几种落地页
    )

    // 是否为付费底层页loid
    fun isPaidAdDetailLoid(loid: Int) = loid == AD_PAID_PUSH_DETAILS

    fun isCommentLoid(loid: Int) = loid in setOf(
        ARTICLE_COMMENT,
        VIDEO_COMMENT,
    )

}

@Deprecated("用 AdVideoConfig.getVideoAdScene")
fun getVideoStreamLoidByFrom(schemeFrom: String): Int {
    return when {
        schemeFrom == SchemeFrom.WEIXIN && getShiplySwitch("ad_plugin_video_rel_stream_switch")
            -> AdLoid.WEIXIN_VIDEO_STREAM

        schemeFrom == SchemeFrom.PUSH && getShiplySwitch("enable_use_loid132")
            -> AdLoid.PUSH_VIDEO_STREAM

        else -> AdLoid.VIDEO_STREAM

    }
}