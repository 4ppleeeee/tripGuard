package com.tencent.news.core.list.api

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.list.model.IKmmFeedsItem


// 信息流标准生命周期（跨端统一）
interface IFeedsActionLifecycle {

    var contextProvider: IContextProvider?

    fun onListScroll(isIdle: Boolean, lastVisibleItem: IKmmFeedsItem?, dx: Int, dy: Int)

    fun onListShow(lastVisibleItem: IKmmFeedsItem)  // 列表可见时触发（从后台回前台、切换列表、从底层返回 等）

    fun onPageSelect(selectedItem: IKmmFeedsItem)   // 在竖版等场景中，被选中时触发（普通list形式没有这个）

    fun onListAttach() {}   // listView上屏时触发（此时未必 onListShow）：可以用于绑定各种监听

    fun onListDetach() {}   // listView销毁/回收时触发：可以用于解绑监听


    @Deprecated("命名容易重复，换：onListAttach")
    fun onAttach() {
        onListAttach()
    }

    @Deprecated("命名容易重复，换：onListDetach")
    fun onDetach() {
        onListDetach()
    }

}

interface IContextProvider {
    fun getContext(): IKmmContext?
}