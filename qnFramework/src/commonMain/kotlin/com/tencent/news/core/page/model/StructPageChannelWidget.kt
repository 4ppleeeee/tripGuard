@file:Suppress("RedundantConstructorKeyword")

package com.tencent.news.core.page.model

import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.list.api.IFlexibleFeedsController
import com.tencent.news.core.setup.LazyImpl
import com.tencent.news.core.util.lifecycle.PageLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow


typealias PageVMCreator = (
    flexCtrl: IFlexibleFeedsController,
    pageFlow: SharedFlow<PageLifecycleEvent>,
    pageScope: CoroutineScope
) -> IStructPageViewModel

interface IStructSubPage {
    val subPageWidget: LazyImpl<StructPageWidget2>
    val subPageVM: PageVMCreator?
}

// 使用 StructPageWidget2 模式创建的子tab；
// 一般用于子tab的接口数据解析都需要特殊实现的情况（比如需要替换 dataRepo）
open class StructPageChannelWidget constructor(
    override val subPageWidget: LazyImpl<StructPageWidget2>,
    override val subPageVM: PageVMCreator? = null,
    override var data: ChannelWidgetData? = null,
    override var dtReport: PageDtReport? = null
) : ChannelWidget(), IStructSubPage