package com.tencent.news.core.list.api

/**
 * 翻页的详细信息，例如合集会有上下的锚点id和上下是否有更多。后续可能还有有页数、页内偏移、翻页条数等
 *
 * Author: joejhzhou
 * Date: 2026/3/24
 */
interface IListPageIndexInfo {

    val prevAnchorId: String?
        get() = null

    val hasPrev: Boolean
        get() = false

    val nextAnchorId: String?
        get() = null

    val hasNext: Boolean
        get() = false

}

class ListPageIndexInfo(
    override val prevAnchorId: String?,
    override val hasPrev: Boolean,
    override val nextAnchorId: String?,
    override val hasNext: Boolean
) : IListPageIndexInfo