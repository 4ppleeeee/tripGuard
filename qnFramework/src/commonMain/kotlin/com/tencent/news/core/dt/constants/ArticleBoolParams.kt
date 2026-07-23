package com.tencent.news.core.dt.constants

/**
 * 文章布尔参数位索引定义
 *
 * 与宿主工程 ArticleBoolParams.java 保持一致
 * 每个常量值代表在二进制位图中的位索引（从第0位开始）
 *
 * 【注意】新增位索引时，必须与宿主工程 ArticleBoolParams.java 保持同步
 */
object ArticleBoolParams {
    const val IS_WECHAT_GREEN_BOOK = 24 // 是否小绿书 (wechat_atype == "1")
    const val IS_VERTICAL_COVER = 26    // 是否竖图封面 (ratio < 1.0)
    const val HAS_TIMELINE_SUMMARY = 30 // 事件脉络文章是否配置摘要
}
