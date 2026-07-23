package com.tencent.news.autoreport

import com.tencent.news.core.dt.constants.DtElementId

/**
 * 虚拟父节点参数包。
 * @param parentEid 虚拟父节点的元素id。对于新闻而言，一般仅在纵向展开模块中使用，所以虚拟父节点元素id默认位[ElementId.ITEM_ARTICLE]
 * @param level 层次，从1开始，1代表父节点，2代表祖父节点，一次类推
 * @param params 父节点参数包
 * @param parentParams 虚拟父节点可无限叠加使用，随着父节点的叠加，对应的level也会自动加1。
 *
 * 纵向展开模块中中只需要通过[ItemKt.bindVirtualParentParams]为孩子节点绑定虚拟父节点即可。
 */
data class VirtualParentParams(
    val parentEid: String = DtElementId.ArticleCard.id,
    val level: Int = 1,
    val params: Map<String, Any>? = null,
    val parentParams: VirtualParentParams? = null,
)