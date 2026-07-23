@file:Suppress("PropertyName", "VariableNaming")

package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable


/**
 * 节点信息（专题节点外显用，6170 加入）；常见使用场景：
 * - 信息流 cell 的公告 marquee 滚动条目（QA 头部大卡 / SkinBigCardHeader）
 *
 * 字段名与后台 JSON 字段保持一致（避免使用 @SerialName，确保 Java/Kotlin 解析一致）。
 */
@Serializable
class KmmNodeContents : IKmmKeep, IKmmParcelable {

    var title: String = ""
    var id: String = ""
    var icon: String = ""
    var timestamp: String = ""
    var tab_id: String = ""
    var rec_icon: String = ""
    var scheme: String = ""
    var image: String = ""

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(title)
        dest.writeString(id)
        dest.writeString(icon)
        dest.writeString(timestamp)
        dest.writeString(tab_id)
        dest.writeString(rec_icon)
        dest.writeString(scheme)
        dest.writeString(image)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        title = from.readString()
        id = from.readString()
        icon = from.readString()
        timestamp = from.readString()
        tab_id = from.readString()
        rec_icon = from.readString()
        scheme = from.readString()
        image = from.readString()
    }

    override fun toString(): String =
        "KmmNodeContents(title='$title', id='$id', icon='$icon', tab_id='$tab_id')"
}
