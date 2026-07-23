package com.tencent.news.core.pay.model

import com.tencent.news.core.parcel.IKmmParcelable

interface IDisplayInfo : IKmmParcelable {
    var title: String
    var desc: String
    var cornerMark: String
    var exposure: String
    var promotionDuration: String
    var cancelDescV2: String
}