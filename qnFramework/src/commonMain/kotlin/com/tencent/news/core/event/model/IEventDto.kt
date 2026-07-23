package com.tencent.news.core.event.model

import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.ip.model.IIpSeasonInfo
import com.tencent.news.core.list.model.CommonBackground
import com.tencent.news.core.list.model.EventTimeLine
import com.tencent.news.core.list.model.IKmmHotEvent
import com.tencent.news.core.list.model.IKmmTimeLineModule
import com.tencent.news.core.list.model.TimelineTab
import com.tencent.news.core.parcel.IKmmParcelable


interface IEventDto : IItemDtoDoc, IKmmKeep, IKmmParcelable {

    /**
     * 专题/事件等集合形式的文章，在信息流上还有一篇外显的焦点文章，这个focusId是焦点文章的id；
     * 这个字段改过名字，之前叫 focusNewsId 但废弃很久了，接入层重新启用后改了个字段名（@since 7120）
     */
    var focusId: String

    var timeLine: IKmmTimeLineModule?
    var thingDisplayCmsId: String
    var ipSeasonInfo: IIpSeasonInfo?

    var hotEvent: IKmmHotEvent?

    var cmsId: String?

    val title: String

    val descSwitch: String?

    val ranking: Int

    var clientTimeLineItem: EventTimeLine?

    var commonBackground: CommonBackground?

    var tabs: List<TimelineTab>?

    // 专题标题
    var speciallistTitle: String
    // 每日报纸标题
    var dailyPaperTitle: String
    // 热点追踪的进展数量
    var hotEventProgressCount: Long
    // 专题头部背景lottie
    var lottie: String

    var relateEventType: String?
}