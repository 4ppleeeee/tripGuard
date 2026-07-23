package com.tencent.news.core.list.extension

import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.list.api.IContextDto
import com.tencent.news.core.list.api.IContextDtoHolder
import com.tencent.news.core.list.model.ArticleType
import com.tencent.news.core.list.model.IFeedsDtoItem
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.list.model.ItemAdEx.isAd
import com.tencent.news.core.list.model.ItemAdEx.isNativeAd
import com.tencent.news.core.list.model.PicShowType
import com.tencent.news.core.platform.api.md5


// 专题场景下专题id
fun IContextDto.getEventId(): String {
    return if (this.pageArticleType == ArticleType.HOT_EVENT) {
        this.pageArticleId
    } else {
        ""
    }
}

fun List<IContextDtoHolder?>?.bindCtxDto(action: IContextDto.() -> Unit) {
    this?.forEach { it.bindCtxDto(action) }
}

fun IContextDtoHolder?.bindCtxDto(action: IContextDto.() -> Unit) {
    this?.findBindingTarget {
        it.ctxDto.action()
    }
}

fun bindPageArticleInfo(target: List<IContextDtoHolder?>?, pageItem: IListItem) {
    target.bindCtxDto {
        pageArticleType = pageItem.baseDto.articleType
        pageArticleId = pageItem.baseDto.idStr
    }
}

fun bindPageArticleId(target: List<IContextDtoHolder?>?, idStr: String) {
    target.bindCtxDto {
        pageArticleId = idStr
    }
}

/**
 * 为文章绑定推荐traceId
 */
fun bindRecTraceId(target: List<IContextDtoHolder?>?, traceId: String?) {
    target.bindCtxDto {
        this.recTraceId = traceId
    }
}

/**
 * 为文章绑定【原始】刷次article_page，其他业务不要用
 */
fun bindCloudRerankArticlePage(target: List<IContextDtoHolder?>?, articlePage: Int) {
    target.bindCtxDto {
        this.cloudRerankArticlePage = articlePage
    }
}

/**
 * 对应大同上报的article_page
 */
fun bindArticlePage(target: List<IContextDtoHolder?>?, articlePage: Int) {
    target.bindCtxDto {
        this.articlePage = articlePage
    }
}

/**
 * 对应大同上报的 article_real_pos 参数，注：广告不占位置
 */
fun bindArticleRealPos(target: List<IContextDtoHolder?>?) {
    if (target.isNullOrEmpty()) {
        return
    }
    var realArticlePos = 1
    target.forEach {
        if (it.ignoreItemPosition()) {
            return@forEach
        }
        if (it.isAd() || it.isNativeAd()) { // ams广告和原生广告，都不占位置
            return@forEach
        }

        it?.findBindingTarget { item ->
            item.ctxDto.articleRealPos = realArticlePos
        }
        realArticlePos++
    }
}

/**
 * 对应大同上报的 article_list_pos 参数，注：广告占位置
 */
fun bindArticleListPos(target: List<IContextDtoHolder?>?) {
    if (target.isNullOrEmpty()) {
        return
    }

    var posInAllData = 1
    target.forEach {
        if (it.ignoreItemPosition()) {
            return@forEach
        }
        it?.findBindingTarget { item ->
            item.ctxDto.articleListPos = posInAllData
        }
        posInAllData++
    }
}

/*
 * 对应大同上报的 article_module_pos 参数
 */
fun bindArticleModulePos(target: List<IContextDtoHolder?>?) {
    if (target.isNullOrEmpty()) {
        return
    }

    target.forEach {
        it?.findBindingTarget { item ->
            if (item is IFeedsDtoItem) {
                val newsList = item.moduleDto.newsModule?.newsList
                newsList?.forEachIndexed { index, newsItem ->
                    newsItem.ctxDto.articleModulePos = index + 1
                }
            }
        }
    }
}

/**
 * 把外层模块item自身的信息（mod*），下沉绑定到模块下嵌套子item的 ctxDto 上。
 * 仅处理 newsModule.newsList 一层嵌套，与 bindArticleModulePos 的处理范围保持一致。
 */
fun bindModArticleInfo(target: List<IContextDtoHolder?>?) {
    if (target.isNullOrEmpty()) {
        return
    }

    target.forEach { holder ->
        if (holder == null) return@forEach
        val moduleItem = holder as? IFeedsDtoItem ?: return@forEach
        val newsList = moduleItem.moduleDto.newsModule?.newsList
        if (newsList.isNullOrEmpty()) {
            return@forEach
        }
        newsList.forEach { newsItem ->
            newsItem.ctxDto.modArticleId = moduleItem.baseDto.idStr
            newsItem.ctxDto.modArticleType = moduleItem.baseDto.articleType
            newsItem.ctxDto.modArticleTitle = moduleItem.baseDto.title
            newsItem.ctxDto.modPicShowType = moduleItem.baseDto.picShowType
        }
    }
}

/**
 * 绑定文章UUID
 */
fun bindArticleUUID(target: List<IContextDtoHolder?>?, channelId: String) {
    if (target.isNullOrEmpty()) {
        return
    }

    target.forEach {
        it?.findBindingTarget { item ->
            if (item !is IKmmFeedsItem) {
                return@findBindingTarget
            }
            item.ctxDto.articleUUID = (channelId + item.getExposureKey()).md5()
        }
    }
}

private fun IContextDtoHolder.findBindingTarget(action: (IContextDtoHolder) -> Unit) {
    // 先处理自己
    action(this)

    // 如果有嵌套item，也绑定一下（这里处理了newsModule）
    if (this is IFeedsDtoItem) {
        this.moduleDto.newsModule?.getContextDtoBindingTargets()?.forEach(action)
    }

    // 后续如果有其他扩展，可以继续加
}

/**
 * 不参与位置计数逻辑的 Item 统一在这里判断
 */
private fun IContextDtoHolder?.ignoreItemPosition(): Boolean {
    var isSpecialItem = false
    // 热问频道只计算头部的位置，不计算中间回答和尾部入口的
    if (this is IFeedsDtoItem) {
        isSpecialItem = this.baseDto.picShowType == PicShowType.QA_CHANNEL_CELL_V3 ||
                this.baseDto.picShowType == PicShowType.QA_CHANNEL_TAIL_V3
    }
    return isSpecialItem || this?.ctxDto?.ignorePos.isTrue()
}
