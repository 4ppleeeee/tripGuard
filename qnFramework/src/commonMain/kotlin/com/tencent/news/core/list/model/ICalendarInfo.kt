package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.isUrl
import com.tencent.news.core.extension.isValidStrColor
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable

interface ICalendarInfo : IKmmKeep {
    var startTime: String
    var startTimeStyle: Int
    var isCenter: Int
    var id: String
    var name: String
    var style: Int
    var titleColor: String
    var columnNameColor: String
    var columnBgImgUrl: String
    var columnBgMaskColor: String
}

@Suppress("ConstructorParameterNaming", "VariableNaming")
@Serializable
class CalendarInfo : BaseKmmModel(), IKmmKeep, ICalendarInfo, IKmmParcelable {
    private var start_time: String = ""
    private var start_time_style: Int = 0
    private var is_center: String = ""
    private var title_color: String = ""
    private var column_name_color: String = ""
    private var column_background_image: String = ""
    private var layer_mask_background_color: String = ""

    override var startTime: String
        get() = start_time
        set(value) {
            this.start_time = value
        }
    override var startTimeStyle: Int
        get() = start_time_style
        set(value) {
            this.start_time_style = value
        }
    override var isCenter: Int
        get() = is_center.toIntOrNull() ?: 0
        set(value) {
            this.is_center = value.toString()
        }
    override var id: String = ""
    override var name: String = ""
    override var style: Int = 0
    override var titleColor: String
        get() = title_color
        set(value) {
            this.title_color = value
        }
    override var columnNameColor: String
        get() = column_name_color
        set(value) {
            this.column_name_color = value
        }
    override var columnBgImgUrl: String
        get() = column_background_image
        set(value) {
            this.column_background_image = value
        }
    override var columnBgMaskColor: String
        get() = layer_mask_background_color
        set(value) {
            this.layer_mask_background_color = value
        }

    fun hasSkin(): Boolean {
        return titleColor.isValidStrColor() && columnNameColor.isValidStrColor() && columnBgMaskColor.isValidStrColor() && isUrl(
            columnBgImgUrl
        )
    }

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(startTime)
        dest.writeInt(startTimeStyle)
        dest.writeInt(isCenter)
        dest.writeString(id)
        dest.writeString(name)
        dest.writeInt(style)
        dest.writeString(titleColor)
        dest.writeString(columnNameColor)
        dest.writeString(columnBgImgUrl)
        dest.writeString(columnBgMaskColor)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        startTime = from.readString()
        startTimeStyle = from.readInt()
        isCenter = from.readInt()
        id = from.readString()
        name = from.readString()
        style = from.readInt()
        titleColor = from.readString()
        columnNameColor = from.readString()
        columnBgImgUrl = from.readString()
        columnBgMaskColor = from.readString()
    }
}