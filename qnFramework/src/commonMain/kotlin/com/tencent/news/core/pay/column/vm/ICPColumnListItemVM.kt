package com.tencent.news.core.pay.column.vm

import com.tencent.news.core.list.model.IItemLabel
import kotlinx.coroutines.flow.StateFlow

/**
 * CP 专栏列表页文章卡片 ViewModel 接口
 * 左图右文布局，图片左上角有"精选"标签
 */
interface ICPColumnListItemVM {
    /** 文章封面图 URL */
    val coverImageUrl: String

    /** 上方标签 */
    val upLabel: IItemLabel?

    /** 是否展示"精选"标签 */
    val showSelectedTag: Boolean

    /** 底部标签列表（付费标签、图文数、阅读数等） */
    val bottomLabels: List<IItemLabel>

    /**
     * 付费关系版本号，每次付费关系变化时递增。
     * UI 侧通过 collectAsState 订阅，触发 Compose 重组以刷新 bottomLabels。
     */
    val paymentVersion: StateFlow<Int>
}
