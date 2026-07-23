package com.tencent.news.qnchannel.api

import com.tencent.news.core.extension.IKmmKeep


interface IIconStyle : IKmmKeep {
    @get:FuncBtnType
    val typeId: String?

    val resourceConfig: IResConfig?

    val webUrl: String?
}