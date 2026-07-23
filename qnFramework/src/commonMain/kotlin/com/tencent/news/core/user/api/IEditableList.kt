package com.tencent.news.core.user.api

import com.tencent.news.core.compose.scaffold.OnDataProcessSuccess
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.platform.i18n.UiText
import com.tencent.news.core.user.model.EditableListState
import kotlinx.coroutines.flow.StateFlow

/**
 * 删除操作的结果
 */
sealed class DeleteResult {
    /** 普通删除成功 */
    data object Normal : DeleteResult()

    /** 全选删除成功（需要额外处理 footer 和数据同步标记） */
    data object SelectAll : DeleteResult()

    /** 推送优化成功（不移除条目，而是置灰 + 退出编辑模式） */
    data class Optimized(val items: List<IKmmFeedsItem>) : DeleteResult()

    /** 删除失败 */
    data class Failed(val error: Throwable) : DeleteResult()
}

/**
 * 可编辑列表的操作接口
 */
interface IEditableList {
    val editState: StateFlow<EditableListState>

    /**
     * 是否在等待数据同步
     * 全选删除后后台数据可能需要时间同步，通过此标记告知后台返回最新数据
     *
     * - 全选删除成功后设置为 true
     * - refresh请求成功后重置为false
     * - reset和下拉刷新请求时读取，若为true则请求参数带上if_get_newest_data=1
     */
    var isWaitingDataSync: Boolean

    // 编辑模式控制
    fun enterEditMode(): Boolean
    fun exitEditMode(): Boolean

    // 选择操作
    fun toggleItemSelection(key: String)
    fun toggleAllSelection(items: List<IKmmFeedsItem>)
    fun resolveKey(item: IKmmFeedsItem): String
    fun updateCheckedKeys(keys: Set<String>)
    fun updateRealCheckedKeys(keys: Set<String>)

    // 选中数据查询
    fun getCheckedItems(items: List<IKmmFeedsItem>): List<IKmmFeedsItem>
    fun getReservedItems(items: List<IKmmFeedsItem>): List<IKmmFeedsItem>

    // 编辑按钮状态
    fun getEditBtnText(): UiText
    fun setEditBtnEnable(enable: Boolean)
    fun getEditBtnDtElementId(): String

    /**
     * 全选模式下分页加载新数据后的回调
     * 子类可重写以实现自动选中新加载的数据等逻辑
     *
     * @param event 分页加载成功事件
     */
    fun onPageLoadedInSelectAllMode(event: OnDataProcessSuccess) {}

    /**
     * 执行删除操作（统一入口）
     * 子类根据自身特性（如是否全选模式）决定删除策略
     */
    suspend fun deleteChecked(allItems: List<IKmmFeedsItem>): DeleteResult
}
