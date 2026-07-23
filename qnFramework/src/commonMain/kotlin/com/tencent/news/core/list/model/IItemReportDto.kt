package com.tencent.news.core.list.model

import com.tencent.news.autoreport.VirtualParentParams
import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * Author: joejhzhou
 * Date: 2025/8/11
 */
interface IItemReportDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    var cctvVideoDurationReportUrl: String  // 央视频视频时长上报url

    // 【大同】额外上报参数（一般是业务特有私参绑定到这里，绑定后就不再变化的固定参数）
    fun getDtExtra(): Map<String, String>
    fun putDtExtra(key: String, value: String?): String?

    // 【大同】动态上报参数（与getDtExtra相比，上报时机不同；后续修改也可以生效）
    fun getDtDynamic(): Map<String, String>
    fun putDtDynamic(key: String, value: String?): String?

    var dtVirtualParent: VirtualParentParams?

}