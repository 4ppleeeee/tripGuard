package com.tencent.news.core.ip.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import kotlinx.serialization.Serializable


@Suppress("AnnotationOnSeparateLine")
typealias QnIpSeasonInfo = @Serializable(IIpSeasonInfo.QnSerializer::class) IIpSeasonInfo

interface IIpSeasonInfo : IKmmKeep {
    val ipId: String?       // 对应的ipid
    val sceneCount: Int     // 共多少集
    val hotVal: Int         // 热度
    val latestSceneInfo: ILatestSceneInfo?  // 最新的一期信息

    object QnSerializer : QnInterfaceSerializer<IIpSeasonInfo>(IIpSeasonInfo::class)

    companion object : IQnInterfaceCreator<IIpSeasonInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}