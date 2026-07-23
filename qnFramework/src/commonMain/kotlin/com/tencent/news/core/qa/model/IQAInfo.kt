package com.tencent.news.core.qa.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnCompatSerializer
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import kotlinx.serialization.Serializable

@Serializable(IKmmQAInfo.QnSerializer::class)
interface IKmmQAInfo : IKmmKeep, IKmmParcelable {

    var question_id: String?
    var approve_num: Int
    var question_title: String?
    var question_num: Int                                       // 问题数（@cell618）
    var answer_num: Int                                         // 回答数（@cell618）

    object QnSerializer : QnCompatSerializer<IKmmQAInfo>(
        qnParser = { QnKmmModelConvert.qaInfoParser },
        kmmSerializer = { KmmQAInfo.serializer() }
    )

    companion object : IQnInterfaceCreator<IKmmQAInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}

@Serializable
open class KmmQAInfo : IKmmQAInfo {

    override var question_id: String? = null
    override var approve_num: Int = 0
    override var question_title: String? = null
    override var question_num: Int = 0
    override var answer_num: Int = 0

    override fun writeToKmmParcel(dest: IKmmParcel) {
        dest.writeString(question_id)
        dest.writeInt(approve_num)
        dest.writeString(question_title)
        dest.writeInt(question_num)
        dest.writeInt(answer_num)
    }

    override fun readFromKmmParcel(from: IKmmParcel) {
        question_id = from.readString()
        approve_num = from.readInt()
        question_title = from.readString()
        question_num = from.readInt()
        answer_num = from.readInt()
    }
}