package com.tencent.news.core.compose.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.news.core.compose.scaffold.card.FeedsItemCtx
import com.tencent.news.core.page.model.ListHeaderWidget
import com.tencent.news.core.page.model.toFeedsItemList
import com.tencent.news.core.service.ViewService

@Composable
fun StructListHeaderView(headerWidget: ListHeaderWidget) {
    val feedsItemList = headerWidget.headerList?.toFeedsItemList() ?: return
    Column(modifier = Modifier.fillMaxWidth()) {
        feedsItemList.forEachIndexed { idx, feedItem ->
            val headerCtx = FeedsItemCtx(indexInList = idx, listSize = feedsItemList.size)
            // item的equals使用id判断，如果是构造的假id，会导致重组失败
            key(headerWidget) {
                ViewService.itemCard.Build(feedItem, headerCtx)
            }
        }
    }
}