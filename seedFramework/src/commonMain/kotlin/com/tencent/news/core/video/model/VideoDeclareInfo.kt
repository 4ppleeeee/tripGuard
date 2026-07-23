@file:Suppress("PrivatePropertyName", "ConstructorParameterNaming")

package com.tencent.news.core.video.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.parcel.IKmmParcelableCreator
import kotlinx.serialization.Serializable

@Serializable
class VideoDeclareInfo(
    var type: String? = null,               // 类型：ai, self_declare, spoiler
    var text: String? = null,               // 文案
    private var self_declare_id: Int = 0,   // 当type=self_declare时，对应的id
) : IKmmKeep, IKmmParcelable {

    var selfDeclareId: Int
        get() = self_declare_id
        set(value) {
            self_declare_id = value
        }

    /**
     * 是否是自行拍摄的自主声明
     */
    fun isSelfPubDeclare(): Boolean {
        return type == VideoDeclareType.SELF_DECLARE.value &&
                selfDeclareId == VideoSelfDeclareId.SELF_PUB.value &&
                !text.isNullOrBlank()
    }

    /**
     * 是否合法：type和text都不为空
     */
    fun valid(): Boolean = !type.isNullOrBlank() && !text.isNullOrBlank()

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(type)
        dest.writeString(text)
        dest.writeInt(selfDeclareId)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        type = from.readString()
        text = from.readString()
        selfDeclareId = from.readInt()
    }

    companion object : IKmmParcelableCreator<VideoDeclareInfo> {
        override fun getKmmParcelClass() = VideoDeclareInfo::class
        override fun createParcelObject() = VideoDeclareInfo()
    }

}

enum class VideoDeclareType(val value: String) {
    /**
     * ai声明
     */
    AI("ai"),

    /**
     * 自主声明
     */
    SELF_DECLARE("self_declare"),

    /**
     * 剧透（也包含旧闻）
     */
    SPOILER("spoiler")
}

enum class VideoSelfDeclareId(val value: Int) {
    /**
     * 自行拍摄
     */
    SELF_PUB(6)
}
