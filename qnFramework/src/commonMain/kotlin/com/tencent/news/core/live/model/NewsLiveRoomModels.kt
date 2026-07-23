package com.tencent.news.core.live.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class NewsLiveRoomIcons(
    @SerialName("16:9") var ratio169: String = "",
    @SerialName("1:1") var ratio11: String = "",
    @SerialName("3:4") var ratio34: String = "",
) : IKmmKeep, IKmmParcelable, IKmmPure {

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(ratio169)
        dest.writeString(ratio11)
        dest.writeString(ratio34)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        ratio169 = from.readString().orEmpty()
        ratio11 = from.readString().orEmpty()
        ratio34 = from.readString().orEmpty()
    }
}

@Serializable
class NewsLiveRoomTab(
    var tab_code: String = "",
    var tab_name: String = "",
    var type: Int = 0,
    var url: String = "",
    var description: String = "",
    var is_default: Int = 0,
    var background_pic: String = "",
) : IKmmKeep, IKmmParcelable {

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(tab_code)
        dest.writeString(tab_name)
        dest.writeInt(type)
        dest.writeString(url)
        dest.writeString(description)
        dest.writeInt(is_default)
        dest.writeString(background_pic)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        tab_code = from.readString().orEmpty()
        tab_name = from.readString().orEmpty()
        type = from.readInt()
        url = from.readString().orEmpty()
        description = from.readString().orEmpty()
        is_default = from.readInt()
        background_pic = from.readString().orEmpty()
    }
}

@Serializable
class NewsLiveRoomLabel(
    var label_id: String = "",
    var label_name: String = "",
    var desc: String = "",
    var url: String = "",
) : IKmmKeep, IKmmParcelable {

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(label_id)
        dest.writeString(label_name)
        dest.writeString(desc)
        dest.writeString(url)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        label_id = from.readString().orEmpty()
        label_name = from.readString().orEmpty()
        desc = from.readString().orEmpty()
        url = from.readString().orEmpty()
    }
}

@Serializable
class NewsLiveRoomVote(
    var id: String = "",
    var name: String = "",
    var url: String = "",
) : IKmmKeep, IKmmParcelable {

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(url)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        id = from.readString().orEmpty()
        name = from.readString().orEmpty()
        url = from.readString().orEmpty()
    }
}

@Serializable
data class RoomPendantInfo(
    /*
        {
            "pendant_id" : 1, // 挂件id
            "pendant_name" : "xxx", // 挂件名称
            "url" : "xxxxx", // 挂件url
            "type" : 1, // 挂件类型，1 native，2 半屏h5
            "icon" : "http://xxx.jpg" // 挂件图片
            "description" : "xxxx" // 挂件描述
            "position" : 1, // 挂件位置, 0 未定义，1 上，2 下
        }
    */
    var pendant_id: Int = 0,
    var pendant_name: String = "",
    var url: String = "",
    var type: Int = 0,
    var icon: String = "",
    var description: String = "",
    var position: Int = 0
) : IKmmKeep, IKmmParcelable {
    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeInt(pendant_id)
        dest.writeString(pendant_name)
        dest.writeString(url)
        dest.writeInt(type)
        dest.writeString(icon)
        dest.writeString(description)
        dest.writeInt(position)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        pendant_id = from.readInt()
        pendant_name = from.readString().orEmpty()
        url = from.readString().orEmpty()
        type = from.readInt()
        icon = from.readString().orEmpty()
        description = from.readString().orEmpty()
        position = from.readInt()
    }

    enum class PendantPosition(val position: Int) {
        TOP(1),
        BOTTOM(2)
    }
}

/**
 * Agent 挂件信息
 * 对应 JSON 字段：
 * {
 *   "name": "观赛",
 *   "url": "https://mat1.gtimg.com/rain/apub2019/xxx.zip",
 *   "scheme": "qqnews://article_9528?act=ai_chat&jumpinfo=...",
 *   "tab_codes": ["chat", "anchor_hall", "description"]
 * }
 */
@Serializable
class NewsLiveAgentPendant(
    var name: String = "", // 挂件名称
    var url: String = "", // 挂件资源地址（如 lottie zip）
    var scheme: String = "", // 点击跳转 scheme
    var tab_codes: List<String> = emptyList(), // 生效的 tab 列表
) : IKmmKeep, IKmmParcelable {

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(name)
        dest.writeString(url)
        dest.writeString(scheme)
        dest.writeList(tab_codes)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        name = from.readString().orEmpty()
        url = from.readString().orEmpty()
        scheme = from.readString().orEmpty()
        tab_codes = from.readList(String::class) ?: emptyList()
    }
}

/**
 * 直播间主题广告（live_theme 数组项）
 * - 入口卡片在直播间右上角自动轮播，每条停留 10s
 * - 多端共用：Android / iOS / 鸿蒙
 * REF: https://yapi.pl.woa.com/project/2418/interface/api/101830
 */
@Serializable
class NewsLiveThemeAd(
    var id: Long = 0L,
    var name: String = "",
    var cover: String = "",
    var page_url: String = "",
    var start_time: Long = 0L,
    var end_time: Long = 0L
) : IKmmKeep, IKmmParcelable {

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeLong(id)
        dest.writeString(name)
        dest.writeString(cover)
        dest.writeString(page_url)
        dest.writeLong(start_time)
        dest.writeLong(end_time)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        id = from.readLong()
        name = from.readString().orEmpty()
        cover = from.readString().orEmpty()
        page_url = from.readString().orEmpty()
        start_time = from.readLong()
        end_time = from.readLong()
    }
}

