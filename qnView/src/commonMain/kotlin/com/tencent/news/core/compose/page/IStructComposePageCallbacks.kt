package com.tencent.news.core.compose.page

import androidx.compose.runtime.Stable
import com.tencent.news.core.compose.scaffold.IStructPageViewModel
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.page.model.StructPageData

@Stable
interface IStructComposePageCallbacks {

    // 页面vm创建完毕后回调
    fun onPageViewModelCreated(vm: IStructPageViewModel) {}

    // 数据拉取结束，准备展示UI之前回调；
    // 返回true表示拦截展示UI（例如：专题不支持的 business_type 做降级）
    fun onBeforeShowMainContent(pageData: StructPageData): Boolean = false

    // 页面UI构建之后回调（pageWidget数据是全的）
    fun onAfterShowMainContent(pageVM: IStructPageViewModel) {}

}


@Stable
interface IStructComposeDataSource {
    fun getPageItem(): IKmmFeedsItem? = null
    fun getCacheKey(): String? = null
}