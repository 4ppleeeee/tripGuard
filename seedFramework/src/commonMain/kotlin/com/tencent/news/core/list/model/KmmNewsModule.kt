package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.list.api.IContextDtoBinding
import com.tencent.news.core.list.api.IContextDtoHolder
import com.tencent.news.core.parcel.IKmmParcel
import com.tencent.news.core.parcel.IKmmParcelable
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.SafeInt
import com.tencent.news.core.serializer.KtJson
import kotlinx.serialization.Serializable


@Suppress("AnnotationOnSeparateLine")
typealias QnKmmNewsModule = @Serializable(IKmmNewsModule.QnSerializer::class) IKmmNewsModule

interface IKmmNewsModule : IKmmKeep, IKmmParcelable, IContextDtoBinding {
    val title: String
    var newsList: QnKmmFeedsItemList
    val itemGroups: List<INewsModuleItemGroup> get() = emptyList()
    val config: INewsModuleConfig?
    val color: INewsModuleColor?

    override fun getContextDtoBindingTargets(): List<IContextDtoHolder>? = newsList

    object QnSerializer : QnCompatSerializer<IKmmNewsModule>(
        qnParser = { QnKmmModelConvert.newsModuleParser },
        kmmSerializer = { KmmNewsModule.serializer() }
    )

    companion object : IQnInterfaceCreator<IKmmNewsModule> {
        override fun defaultSerializer() = QnSerializer

        fun new(newsList: MutableList<IListItem>) = new {
            this.newsList = newsList
        }

        fun createEmptyInstance(): IKmmNewsModule = IKmmNewsModule.new()
    }

}

interface INewsModuleConfig {
    var moduleTitle: String
    var actionBarTitle: String
    var moduleIcon: String
    var moduleIconNight: String
    var icon_type: SafeInt
    var scrollType: String // 是否开启轮播
}

interface INewsModuleColor {
    var topic: String
    var backgroundDay: String
    var backgroundNight: String
}

interface INewsModuleItemGroup : IKmmKeep {
    val group_type: String?
    val group_title: String?
    val ids: List<String>?
}

@Serializable
class KmmNewsModule : BaseKmmModel(), IKmmNewsModule {

    override val title: String = ""
    internal var newslist: QnKmmFeedsItemList = null
    override var newsList: QnKmmFeedsItemList
        get() = newslist
        set(value) {
            newslist = value
        }

    private var item_groups: List<NewsModuleItemGroup>? = null
    override val itemGroups: List<INewsModuleItemGroup>
        get() = item_groups.orEmpty()

    internal var moduleConfig: ModuleConfig? = null
    override val config: INewsModuleConfig? get() = moduleConfig

    internal var moduleColor: ModuleColor? = null
    override val color: INewsModuleColor?
        get() = moduleColor

    override fun writeToKmmParcel(dest: IKmmParcel) {

    }

    override fun readFromKmmParcel(from: IKmmParcel) {

    }

    companion object {
        fun fromJson(json: String?): KmmNewsModule =
            KtJson.safeDecode<KmmNewsModule>(json) ?: KmmNewsModule()
    }
}

@Serializable
class NewsModuleItemGroup : BaseKmmModel(), INewsModuleItemGroup {
    override var group_type: String? = null
    override var group_title: String? = null
    override var ids: List<String>? = null
}

@Serializable
internal class ModuleConfig : IKmmKeep, INewsModuleConfig {
    override var moduleTitle: String = ""
    override var moduleIcon: String = ""
    override var moduleIconNight: String = ""
    override var icon_type: SafeInt = 0
    override var scrollType: String = ""
    override var actionBarTitle: String = ""
}


@Serializable
internal class ModuleColor : IKmmKeep, INewsModuleColor {
    override var topic: String = ""
    override var backgroundDay: String = ""
    override var backgroundNight: String = ""
}
