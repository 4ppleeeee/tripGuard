package com.tencent.news.core.tads.model

import com.tencent.news.core.tads.constants.AdJumpAction
import com.tencent.news.core.tads.constants.AdJumpLinkInfoData
import com.tencent.news.core.tads.constants.AdJumpLinkMap


interface IAdMdpaItem {

    val templateImageUrl: String

    val title: String
    val isNewProduct: Boolean
    val description: String         // 商品描述
    val attributes: List<String>    // 商品标签

    val price: String
    val originalPrice: String

    // 跳转相关：
    val clickUrl: String
    val schemaUrl: String
    val wxMiniProgramPath: String
    val universalLink: String       // iOS专用

    // jump-actions
    val jumpActions: List<AdJumpAction>?

    // v2 新链路
    val linkInfoMap: AdJumpLinkMap?
    val jumpDataList: List<AdJumpLinkInfoData>?

}