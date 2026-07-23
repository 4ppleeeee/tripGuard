package com.tencent.news.core.list.constants

import com.tencent.news.core.extension.IListEnumDoc


/**
 * 触发列表刷新的用户操作类型
 *
 * @see ListRefreshForward
 */
enum class ListRefreshAction(val code: Int) : IListEnumDoc {

    NONE(-1),               // 初始值
    INIT_CACHE(-2),         // 初始化时读取文件缓存

    PULL_DOWN(1),           // 下拉刷新
    CLICK_DIV(2),           // 点击分隔条（会下拉刷新）
    PULL_UP(3),             // 上拉刷新（拖拽底部加载条）
    CLICK_RETRY(4),         // 点击重试页
    TAB_UP(5),              // 上拉刷新（点击底部加载条）
    AUTO_MORE(6),           // 上拉刷新（滑动时自动触发）
    FORE_GROUND(7),
    PAGE_SHOWUP(8),
    RESET_CHANNEL(9),       // reset刷新
    CLICK_TAB(10),          // 点击页卡
    CLICK_CHANNEL_BAR(11),  // 点击导航条（会下拉刷新）
    CLICK_NEWS_LOGO(12),    // 点击左上角Logo(触发下拉刷新）
    QUERY_EXPANSION(14),    // 展开全部
    REMOVE_RECOMMEND(15),   // 去掉推荐内容
    LOAD_RECOMMEND(16),     // 展示推荐内容

    RESET_BY_CLICK_LAST_READ(17),   // 点击上次阅读位置小条的reset
    RESET_TOPIC_EVENT_LATEST(18),   // 点击话题专题内容切换（最新/最热）
    RESET_TOPIC_EVENT_HOTTEST(19),  // 点击话题专题内容切换（最新/最热）

    SCROLL_TOP_MORE(20),    // 由滑动操作触发的顶部自动加载（付费专栏 用到这个交互）

    CLOUD_REPLACE(21),      // 云替换
    TAB2_CLOUD_REPLACE(28), // tab2/沉浸式云重排替换
    MIXED_LANDING_CLOUD_REPLACE(30), // 混合流落地页云重排替换

    PRELOAD(22),            // 预加载

    AUTO_CACHE(23),         // 第一次构建列表时自动触发缓存

    CACHE_AFTER_RESET(24),  // 第一次获取缓存后触发reset

    RESET_ENTER_FOREGROUND(25),     // 进入前台后触发的自动刷新

    CHANNEL_724_ONLY_IMPORTANT(26),     // 724频道只刷新重要内容
    CHANNEL_724_CUSTOMIZE_CATEGORY(27),     // 724频道自定义tag分类

    PAGE_INDEX(29),     // 点击页码组件（IndexWidget）触发的 reset 请求

    ;

    override fun toString(): String {
        return code.toString()
    }

}

object ListRefreshActionEx {
    fun getByCode(code: Int): ListRefreshAction {
        return when (code) {
            // 正则：
            // (\w+)\((.+)\).*
            // ListRefreshAction.$1.code -> ListRefreshAction.$1

            ListRefreshAction.INIT_CACHE.code -> ListRefreshAction.INIT_CACHE

            ListRefreshAction.PULL_DOWN.code -> ListRefreshAction.PULL_DOWN
            ListRefreshAction.CLICK_DIV.code -> ListRefreshAction.CLICK_DIV
            ListRefreshAction.PULL_UP.code -> ListRefreshAction.PULL_UP
            ListRefreshAction.CLICK_RETRY.code -> ListRefreshAction.CLICK_RETRY
            ListRefreshAction.TAB_UP.code -> ListRefreshAction.TAB_UP
            ListRefreshAction.AUTO_MORE.code -> ListRefreshAction.AUTO_MORE
            ListRefreshAction.FORE_GROUND.code -> ListRefreshAction.FORE_GROUND
            ListRefreshAction.PAGE_SHOWUP.code -> ListRefreshAction.PAGE_SHOWUP
            ListRefreshAction.RESET_CHANNEL.code -> ListRefreshAction.RESET_CHANNEL
            ListRefreshAction.CLICK_TAB.code -> ListRefreshAction.CLICK_TAB
            ListRefreshAction.CLICK_CHANNEL_BAR.code -> ListRefreshAction.CLICK_CHANNEL_BAR
            ListRefreshAction.CLICK_NEWS_LOGO.code -> ListRefreshAction.CLICK_NEWS_LOGO
            ListRefreshAction.QUERY_EXPANSION.code -> ListRefreshAction.QUERY_EXPANSION
            ListRefreshAction.REMOVE_RECOMMEND.code -> ListRefreshAction.REMOVE_RECOMMEND
            ListRefreshAction.LOAD_RECOMMEND.code -> ListRefreshAction.LOAD_RECOMMEND

            ListRefreshAction.RESET_BY_CLICK_LAST_READ.code -> ListRefreshAction.RESET_BY_CLICK_LAST_READ
            ListRefreshAction.RESET_TOPIC_EVENT_LATEST.code -> ListRefreshAction.RESET_TOPIC_EVENT_LATEST
            ListRefreshAction.RESET_TOPIC_EVENT_HOTTEST.code -> ListRefreshAction.RESET_TOPIC_EVENT_HOTTEST

            ListRefreshAction.SCROLL_TOP_MORE.code -> ListRefreshAction.SCROLL_TOP_MORE

            ListRefreshAction.CLOUD_REPLACE.code -> ListRefreshAction.CLOUD_REPLACE
            ListRefreshAction.TAB2_CLOUD_REPLACE.code -> ListRefreshAction.TAB2_CLOUD_REPLACE
            ListRefreshAction.MIXED_LANDING_CLOUD_REPLACE.code -> ListRefreshAction.MIXED_LANDING_CLOUD_REPLACE
            ListRefreshAction.CHANNEL_724_ONLY_IMPORTANT.code -> ListRefreshAction.CHANNEL_724_ONLY_IMPORTANT
            ListRefreshAction.CHANNEL_724_CUSTOMIZE_CATEGORY.code -> ListRefreshAction.CHANNEL_724_CUSTOMIZE_CATEGORY

            ListRefreshAction.PAGE_INDEX.code -> ListRefreshAction.PAGE_INDEX

            else -> ListRefreshAction.NONE
        }
    }
}

fun ListRefreshAction.isCloudReplaceAction(): Boolean {
    return this == ListRefreshAction.TAB2_CLOUD_REPLACE ||
        this == ListRefreshAction.MIXED_LANDING_CLOUD_REPLACE
}
