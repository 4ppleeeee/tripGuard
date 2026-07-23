package com.tencent.news.core.list.controller

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.extension.clearAndAddAll
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.safeAddAll
import com.tencent.news.core.extension.safeGet
import com.tencent.news.core.extension.safeSubList
import com.tencent.news.core.list.constants.ListRefreshAction
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.trace.NewsChannelLog
import com.tencent.news.core.page.model.NewsListWidget
import com.tencent.news.core.page.model.StructPageWidget
import com.tencent.news.core.page.model.StructWidget
import com.tencent.news.core.page.model.toFeedsItemList
import com.tencent.news.core.tads.api.IAdFeedsController
import com.tencent.news.core.tads.controller.AdFeedsSectionHelper.bindSectionAdUiBlock
import com.tencent.news.core.tads.model.IKmmAdFeedsItem
import com.tencent.news.core.tads.model.getAdOrder

/**
 * 灵活列表辅助工具：构建结果的item列表
 */
internal object FeedsItemListBuilder {

    fun buildItemListResult(
        requestEnv: FeedsRequestEnv,
        rootPageWidget: StructPageWidget,
        originNewData: List<IKmmFeedsItem>?,
        adCtrl: IAdFeedsController?,
        processor: IFeedsDataProcessor,
    ): FeedsProcessResult {
        val allData = mutableListOf<IKmmFeedsItem>()
        val newData = mutableListOf<IKmmFeedsItem>()
        newData.safeAddAll(originNewData)

        val distinctFilter = mutableSetOf<String>()

        val localFixTopList = rootPageWidget.localFixTopList
        if (!localFixTopList.isNullOrEmpty()) {
            // 置顶文章要与列表进行排重，优先保留置顶文章
            distinctFilter.addAll(localFixTopList.map { getItemKey(it) })
        }

        if (requestEnv.dataEnv.isStructChannel()) {
            // 频道中，headerItems拼接到数据顶端；复用原有的 moveToHeader 能力进行置顶（主要是724频道在用）
            val headerItems = rootPageWidget.getHeaderListWidgets().toFeedsItemList()
            headerItems.forEach { item ->
                item.ctxDto.moveToHeader = true
//                allData.add(item) // todo genesisli dev 后续724要处理下这个置顶能力
                distinctFilter.add(getItemKey(item)) // Header也要和列表数据进行排重
            }
        }

        rootPageWidget.getMainContentWidgets().forEachIndexed { index, widget ->

            if (widget is NewsListWidget) {
                processor.onPreProcessListWidget(requestEnv, widget)
            }

            // 分区原始下发数据
            val sectionOriginList = listOf(widget).toFeedsItemList()
                .filter { !it.isForbidInsert().succeed }

            // 分区加工后的数据（排重、拼接头尾等）
            val sectionFinalList = mutableListOf<IKmmFeedsItem>()

            // todo genesisli dev: preProcessCacheComment 先添加网友热议假评论


            if (sectionOriginList.isEmpty()) {
                // 【重要】特殊逻辑：如果有列表分区需要延迟加载，为了防止列表跳动，在该模块未加载成功前，屏蔽后续内容展示
                if (widget is NewsListWidget && widget.canLazyInit()) {
                    return FeedsProcessResult(allData, newData)
                }
            } else {

                // todo genesisli opt: header 和 footer 统一由kmm创建，两端picShowType统一

                // todo genesisli dev: onBeforeProcessListData 网友热议模块前添加cache中的假评论

                // todo genesisli dev: isQaSection, putExtraReportParam mod_question

                // step 1：拼接分区header（下发section数据才有）
                if (widget is NewsListWidget) {
                    sectionFinalList.safeAddAll(processor.onCreateSectionHeaders(index, widget))
                }
                // todo genesisli dev: 给QaSection绑定上报参数：bindVirtualParentParams

                // step 2：检查排重并添加文章列表
                if (!enableDistinctFilter(widget)) {
                    sectionFinalList.addAll(sectionOriginList)
                } else {
                    sectionOriginList.forEach { item ->
                        val itemKey = getItemKey(item)
                        if (!distinctFilter.contains(itemKey)) {
                            sectionFinalList.add(item)
                            distinctFilter.add(itemKey)
                        } else {
                            newData.remove(item)
                            NewsChannelLog.fileLog("", "排重文章：${item.baseDto.idStr}")
                        }
                    }
                }

                // step 3：拼接分区footer（能点击展开分页时才有）
                if (widget is NewsListWidget && widget.canClickLoadMore()) {
                    sectionFinalList.safeAddAll(processor.onCreateSectionFooters(index, widget))
                }

                // todo genesisli dev: 查证官类型尾部增加"我要提问"


                if (widget is NewsListWidget) {
                    // 如果分区中禁止插入广告，标记一下广告槽位（用最后一个item作展位，整个分区占1个位置）
                    widget.bindSectionAdUiBlock(sectionFinalList)

                    // 绑定section分区信息（影响header-footer圆角样式、目录索引等逻辑）
                    widget.bindSectionInfo(sectionFinalList)
                }


                // 【重要】拼接模块分区结果数据
                allData.addAll(sectionFinalList)
            }
        }

        // 【重要】广告-插入
        adCtrl?.insertFeedsAd(allData, newData)

        // 广告插入后，对外回调的newData也要跟着变（newData里也包含新一刷的广告）
        if (requestEnv.getRefreshAction() != ListRefreshAction.QUERY_EXPANSION) {
            updateNewDataWithAd(allData, newData)
        }

        // debug用：标识重构的新列表
        allData.forEach {
            it.ctxDto.isFlexList = true
            (it as? IKmmAdFeedsItem)?.getAdOrder()?.env?.isFlexList = true
        }

        // 构建结果列表时，给宿主一个修改的机会
        processor.onBuildItemListResult(requestEnv, allData, newData)

        adCtrl?.onAfterInsertFeedAd(requestEnv.dataEnv.refreshForward.code, allData, newData)

        return FeedsProcessResult(allData, newData)
    }

    // 广告插入后，对外回调的newData也要跟着变（newData里也包含新一刷的广告）
    @OptIn(KmmInternalApi::class)
    private fun updateNewDataWithAd(
        allData: MutableList<IKmmFeedsItem>,
        newData: MutableList<IKmmFeedsItem>,
    ) {
        val firstNewItem = newData.firstOrNull() ?: return
        val firstIndex = allData.indexOf(firstNewItem)

        val newDataWithAd = allData.safeSubList(firstIndex, nullIfInvalid = true)
        if (!newDataWithAd.isNullOrEmpty()) {
            newData.clearAndAddAll(newDataWithAd)

            // 特殊处理一下，新一刷广告插入到首篇的情况，也要算在newData里
            val preItem = allData.safeGet(firstIndex - 1)
            if (preItem != null && preItem.ctxDto.tmpIsRefreshNewData) {
                newData.add(0, preItem)
            }
        }

        // 刷新最新标记：
        allData.forEach { it.ctxDto.tmpIsRefreshNewData = false }
    }

    private fun NewsListWidget.bindSectionInfo(sectionItemList: List<IKmmFeedsItem>) {
        val widget = this

        // 先全量清空（模块可能点击footer展开，导致之前绑定的标识不准，需要重绑）
        sectionItemList.forEach {
            it.ctxDto.isSectionHeader = false
            it.ctxDto.isSectionFooter = false
        }

        // todo genesisli dev: 宿主 markEventSectionHead 改成从 ctxDto 取
        // 重新标识模块头尾
        val firstItem = sectionItemList.firstOrNull()
        val lastItem = sectionItemList.lastOrNull()
        if (firstItem != null && lastItem != null) {
            firstItem.ctxDto.isSectionHeader = true
            lastItem.ctxDto.isSectionFooter = true
        }

        // todo genesisli dev: 宿主 SearchConstants.PARAM_SECTION_NAME 改成从 ctxDto 取
        // 绑定所属分区（目录导航时会用这个索引）
        sectionItemList.forEach { item ->
            item.ctxDto.sectionName = widget.data?.section?.name.getNonNull()
        }
    }

    private fun getItemKey(item: IKmmFeedsItem): String = item.baseDto.idStr

    private fun enableDistinctFilter(widget: StructWidget): Boolean {
        if (widget is NewsListWidget) {
            return widget.action?.forbidDistinctFilter != true
        }
        return true
    }

}