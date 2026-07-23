package com.tencent.news.core.page.model

import com.tencent.news.core.vm.IFeedsVMItemStub

// vm想感知header折叠事件，可以实现这个接口
// todo genesisli opt: 目前只给TitleHanging发了，其余的有需要后续可以加
interface IStructHeaderAware {
    fun onHeaderCollapseChanged(isCollapsed: Boolean)
}

// 适用于：vm中想要获取列表item，用于组件间通信（需使用 asItemVM）
interface IFeedsVMItemAware {
    fun onInjectFeedsVMItem(item: IFeedsVMItemStub)
}

// 适用于 VerticalPagerListConfig 列表，cell选中时派发给vm（需使用 asItemVM）
// 判定见：feedsItemCtx.checkCellSelected()
interface IVerticalPagerCellAware {
    fun onPagerCellSelected()

    fun onPagerCellStableSelected() {}
}
