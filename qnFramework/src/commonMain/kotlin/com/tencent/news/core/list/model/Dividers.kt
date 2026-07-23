package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep

object Dividers {

    val DEF = Divider(0.5F, false, 0, 2)

    // 不显示
    val NONE = Divider(0F, false, 1)

    // 细分割线
    val THIN = Divider(0.5F, false, 2)

    // 粗分割线
    val WIDE = Divider(27F, false, 3)

    // 强制不显示
    val FORCE_NONE = Divider(0F, false, 4)

    // 强制细分割线
    val FORCE_THIN = Divider(0.5F, false, 5)

    // 强制粗分割线
    val FORCE_WIDE = Divider(27F, false, 6)

    // 附近不许有任何分割线
    val NOT_ANY_WHERE = Divider(0F, false, 999)

}

typealias DividerType = Int

data class Divider(
    // 分割线高度，单位dp
    val height: Float,
    // 分割线是否通栏
    val isFullWidth: Boolean,
    // 分割线类型
    val type: DividerType,
    // 优先级
    val priority: Int = type,
) : IKmmKeep

fun DividerType.toDivider(): Divider {
    return when (this) {
        Dividers.NONE.type -> Dividers.NONE
        Dividers.THIN.type -> Dividers.THIN
        Dividers.WIDE.type -> Dividers.WIDE
        Dividers.FORCE_NONE.type -> Dividers.FORCE_NONE
        Dividers.FORCE_THIN.type -> Dividers.FORCE_THIN
        Dividers.FORCE_WIDE.type -> Dividers.FORCE_WIDE
        Dividers.NOT_ANY_WHERE.type -> Dividers.NOT_ANY_WHERE
        else -> Dividers.DEF
    }
}
