package com.tencent.news.core.tads.controller

import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.NewsListWidget


object AdFeedsSectionHelper {

    /**
     * 专题-广告模块分区，插入位置避让工具；
     * [sectionItemList] 由外部传入是因为宿主可能有插入header-footer的这些逻辑
     */
    fun NewsListWidget.bindSectionAdUiBlock(sectionItemList: List<IKmmFeedsItem>) {
        val section = this.data?.section
            ?: return

        val closeAdForSection = section.sectionAdSwitch != 1
        val lastItem = sectionItemList.lastOrNull()
        if (closeAdForSection) {
            // 先统一禁掉广告占位
            sectionItemList.forEach { it.adDto.skipAdInsertLoc = true }
            if (canAutoLoadMore()) {
                // 如果分区模块可以自动加载（无限刷类型），则整个模块都不插入广告；保持现在的禁用逻辑
            } else {
                // 如果分区模块不是无限刷类型，则整个模块算1个位置；广告插入到模块之后
                // （把最后一个item禁用逻辑去掉，占1个位置）
                lastItem?.adDto?.skipAdInsertLoc = false
                lastItem?.adDto?.fixAdUiBlockNum = 1
            }
        }
    }

}