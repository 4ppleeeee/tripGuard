package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IAdOrderDtoDoc
import com.tencent.news.core.extension.IItemDtoDoc
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.parcel.IKmmParcelable
import kotlinx.serialization.Serializable


@Suppress("AnnotationOnSeparateLine")
typealias QnTestDto = @Serializable(ITestDto.QnSerializer::class) ITestDto

interface ITestDto : IItemDtoDoc, IAdOrderDtoDoc, IKmmKeep, IKmmParcelable {
    val desc: String                // 注释信息
    val adFile: String              // 用于mock的底层页广告数据（不填会默认用 integration_detail_ad.json）
    val contentFile: String         // 用于mock的底层页内容数据（不填则不mock）
    val forbidJumpTab2: Boolean     // 视频不跳转tab2
    val noFreqLimit: Boolean        // 跳过广告频控
    val ignoreAllFreqLimit: Boolean // 忽略所有频控、开关限制
    var debugErrorInfo: String      // 本地可以绑定的额外错误信息，会展示到小红字里

    object QnSerializer : QnInterfaceSerializer<ITestDto>(ITestDto::class)

    companion object : IQnInterfaceCreator<ITestDto> {
        override fun defaultSerializer() = QnSerializer
    }
}