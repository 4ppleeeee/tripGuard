package com.tencent.news.qnchannel.api

import com.tencent.news.core.extension.IKmmKeep


/**
 * 频道实体相关信息（如tag信息等）（在 [IChannelInfo] 的基础上剥离出来的，为了保持接口精简）
 */

interface IEntityInfo : IKmmKeep {
    var entityId: String?
    val entityGroup: String?
    val groupType: Int
    val showOrder: Int
}