package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 直播主播 VIP 信息接口
 * 对应 JSON 字段：
 * {
 *   "vip_type": 20006,
 *   "vip_type_new": 20006,
 *   "vip_icon": "https://...",
 *   "vip_icon_night": "https://...",
 *   "vip_place": "left",
 *   "vip_desc": ""
 * }
 */
interface INewsLiveVipInfo : IKmmKeep, IKmmParcelable {
    var vip_type: Int
    var vip_type_new: Int
    var vip_icon: String
    var vip_icon_night: String
    var vip_place: String
    var vip_desc: String
}

