package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tads.constants.AdJumpLinkInfoData
import com.tencent.news.core.tads.constants.AdJumpLinkMap


interface IAdResItem : IKmmKeep {

    val url: String

    val clickUrl: String

    val title: String

    val schemeUrl: String // 跳转scheme

    val descriptionText: String // 描述

    val bgUrl: String // 背景图

    val gameName: String // 游戏名称

    val actionBtnText: String // 操作按钮文案

    val gameId: String // 游戏ID

    val linkInfoMap: AdJumpLinkMap?

    val jumpDataList: List<AdJumpLinkInfoData>?

}