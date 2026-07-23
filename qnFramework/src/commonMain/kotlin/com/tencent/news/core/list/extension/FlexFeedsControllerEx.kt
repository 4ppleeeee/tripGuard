package com.tencent.news.core.list.extension

import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.compose.scaffold.vm.StructPageViewModel
import com.tencent.news.core.list.api.IFlexibleFeedsController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow

internal object FlexFeedsControllerEx {

    // 默认的pageVM，pageFlow和pageScope可能不准；
    // 主要给宿主逻辑层使用，控制列表刷新等操作
    fun IFlexibleFeedsController.createDefaultPageVM(): IStructPageViewModel {
        return StructPageViewModel(
            controller = this,
            pageFlow = MutableSharedFlow(),
            pageScope = CoroutineScope(Dispatchers.Main)
        )
    }

}