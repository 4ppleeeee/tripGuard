package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

/**
 * 多路流信息项接口
 * 对应 JSON 字段：
 * {
 *   "seat_id": "13915682",
 *   "stream_id": "1391568200",
 *   "room_id": "0",
 *   "name": "1",
 *   "pic": {
 *     "161x225": "https://...",
 *     "162x92": "https://...",
 *     ...
 *   },
 *   "hv_direction": 1,
 *   "pay_status": 8,
 *   "caption_status": 0,
 *   "caption_show_type": 0
 * }
 */
interface INewsLiveMultiStreamItem : IKmmKeep, IKmmParcelable {
    var seat_id: String
    var stream_id: String
    var room_id: String
    var name: String
    var pic: Map<String, String>
    var hv_direction: Int
    var pay_status: Int
    var caption_status: Int
    var caption_show_type: Int
}

