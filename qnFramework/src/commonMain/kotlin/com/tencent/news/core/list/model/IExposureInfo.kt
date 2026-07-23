package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable

interface IExposureInfo : IKmmParcelable, IKmmKeep {
    var should_report_exposure: Boolean
    var exposure_type: Int
    var exposure_id: String

    object QnSerializer : QnInterfaceSerializer<IExposureInfo>(IExposureInfo::class)

    companion object : IQnInterfaceCreator<IExposureInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}

