@file:Suppress("AnnotationOnSeparateLine")

package com.tencent.news.core.list.model

import com.tencent.news.core.detail.model.IDetailAttribute
import com.tencent.news.core.extension.ICmsModelDoc
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.api.IContextDtoHolder
import com.tencent.news.core.list.api.IExportModel
import com.tencent.news.core.list.api.IExposure
import com.tencent.news.core.list.controller.IFeedsItemValidator
import com.tencent.news.core.page.model.SafeItemListSerializer
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.setup.GlobalModelSerializerFactory
import com.tencent.news.core.view.ILogicContextHolder
import com.tencent.news.core.vm.IFeedsVMItemStub
import kotlinx.serialization.Serializable


/**
 * 由于 item 逐步迁移，当前解析的字段可能不全，需要保存一份后台原始下发的 json 数据；
 * 使用这个解析器可以拦截 [KmmFeedsItem] 的解析，并保留原始 json；
 *
 * 用法类似：
 * var newslist: List<@Serializable(OriginJsonKmmFeedsItemSerializer::class) KmmFeedsItem>? = null
 */
@Deprecated("建议用：QnListItem，且只在model类解析时需要这个")
typealias QnKmmFeedsItem = QnListItem

@Deprecated("建议用：List<QnListItem>，且只在model类解析时需要这个")
typealias QnKmmFeedsItemList = @Serializable(SafeItemListSerializer::class) MutableList<QnListItem>?

// 以前命名不太好，改一套名字
typealias IListItem = IKmmFeedsItem
typealias QnListItem = @Serializable(IKmmFeedsItem.QnSerializer::class) IKmmFeedsItem

/**
 * 信息流文章item
 */
interface IKmmFeedsItem :
    IKmmIndexItem,          // 信息流业务统一的标记接口，目前无实际功能意义
    IFeedsVMItemStub,       // 所有UI层的vm放这里
    ILogicContextHolder,    // 支持逻辑层数据绑定
    IFeedsDtoItem,          // 所有原始数据的dto放这里
    IExposure,              // 支持曝光排重
    IContextDtoHolder,      // 支持客户端本地参数绑定
    IFeedsItemValidator,    // 支持列表数据过滤
    IOriginJson,            // 解析后保留原始json
    ICmsModelDoc,
    IKmmParcelable,
    IDetailAttribute,
    IExportModel {

    override fun getExposureKey(): String {
        return listOf(
            baseDto.idStr,
            baseDto.articleType,
            baseDto.picShowType,
            traceDto.seqNo,
            traceDto.recommendReason,
            traceDto.algVersion
        ).joinToString("_")
    }

    object QnSerializer : QnCompatSerializer<IKmmFeedsItem>(
        qnParser = { QnKmmModelConvert.itemParser },
        kmmSerializer = { GlobalModelSerializerFactory.getDefault() },
        enableBaseItemParser = true // 给ios优化用的
    )

    companion object : IQnInterfaceCreator<IKmmFeedsItem> {
        override fun defaultSerializer() = QnSerializer

        fun safeDecode(json: String?): IKmmFeedsItem? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IKmmFeedsItem): String = KtJson.safeEncode(QnSerializer, data)

        // 实例化一个item，会走宿主的 qnParser；合法性校验：id、articleType 不能空
        fun create(idStr: String, articleType: String): IKmmFeedsItem = IKmmFeedsItem.new {
            baseDto.idStr = idStr
            baseDto.articleType = articleType
        }

    }
}