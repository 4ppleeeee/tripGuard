package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.extension.ICmsModelDoc
import kotlinx.serialization.Serializable

/**
 * 赛事信息跨平台接口
 */
interface IKmmRaceInfo : IKmmKeep, IKmmParcelable, ICmsModelDoc {
    var atnick: String?
}

/**
 * 赛事信息 KMM 实现类
 */
@Serializable
open class RoseRaceInfo : IKmmRaceInfo, IKmmKeep, IKmmParcelable {
    
    override var atnick: String? = null
    
    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(atnick)
    }
    
    override fun readFromKmmParcel(from: IKmmParcel) {
        atnick = from.readString()
    }
}
