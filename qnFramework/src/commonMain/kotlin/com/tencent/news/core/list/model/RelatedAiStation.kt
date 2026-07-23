package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.IKmmPure
import com.tencent.news.core.serializer.SafeInt
import kotlinx.serialization.Serializable


/**
 * 关联AI电台信息
 * 用于表示音频数据所关联的AI电台
 */
interface IRelatedAiStation : IKmmKeep {
    val stationId: String                                    // 电台ID
    val stationTitle: String                                 // 电台标题
    val stationDesc: String                                  // 电台描述
    val subAType: Int                                        // 子类型，401表示该topic有合集
    val collectionTitle: String                              // 合集标题
}

@Suppress("ConstructorParameterNaming", "VariableNaming")
@Serializable
class RelatedAiStation : BaseKmmModel(), IRelatedAiStation, IKmmPure {

    private var id: String = ""
    private var title: String = ""
    private var desc: String = ""
    private var sub_atype: SafeInt = 0
    private var collection_title: String = ""

    override val stationId: String get() = id

    override val stationTitle: String get() = title

    override val stationDesc: String get() = desc

    override val subAType: Int get() = sub_atype

    override val collectionTitle: String get() = collection_title

}
