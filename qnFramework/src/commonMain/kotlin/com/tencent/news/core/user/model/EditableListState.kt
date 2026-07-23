package com.tencent.news.core.user.model

import com.tencent.news.core.dt.constants.DtElementId
import com.tencent.news.core.platform.i18n.UiText

/**
 * 可编辑列表的状态
 */
data class EditableListState(
    val isEditing: Boolean = false,              // 是否处于编辑模式
    val checkedKeys: Set<String> = emptySet(),   // 已选中的 item key 集合
    val realCheckedKeys: Set<String> = emptySet(), // 恒久选中的 item key 集合（不可再次切换）
    val editBtnEnable: Boolean = false,          // 编辑按钮是否可用
    val isSelectAll: Boolean = false,            // 是否处于全选模式
    val deleteBtnText: UiText? = null,              // 删除按钮文本
    val deleteBtnElementId: DtElementId? = null, // 删除按钮dt元素id
)