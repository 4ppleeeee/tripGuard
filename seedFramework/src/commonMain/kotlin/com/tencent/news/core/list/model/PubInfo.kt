package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable

interface IPubInfo : IKmmParcelable, IKmmKeep {
    /**
     * 文章发布来源
     */
    var source: String?

    /**
     * 文章发布子来源
     */
    var subSource: String?

    /**
     * 判断是否是微信公众号来的文章
     */
    fun isWeiXinArticle(): Boolean {
        return "1" == source && "120" == subSource
    }

    object QnSerializer : QnInterfaceSerializer<IPubInfo>(IPubInfo::class)

    companion object : IQnInterfaceCreator<IPubInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}

@Suppress("ConstructorParameterNaming", "VariableNaming")
@Serializable
class PubInfo : BaseKmmModel(), IPubInfo, IKmmKeep {

    private var sub_source: String? = null

    override var source: String? = null

    override var subSource: String?
        get() = sub_source
        set(value) {
            this.sub_source = value
        }

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(source)
        dest.writeString(subSource)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        source = from.readString()
        subSource = from.readString()
    }
}