package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable


@Serializable
class TimelineTab : BaseExposureKmmModel(), IKmmKeep, IKmmParcelable {
    var id: String = ""
    var tab: String = ""
    var count: Int = 0

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(id)
        dest.writeString(tab)
        dest.writeInt(count)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        id = from.readString()
        tab = from.readString()
        count = from.readInt()
    }

    override fun toString() = "TimelineTab(id='$id', tab='$tab', count=$count)"

}