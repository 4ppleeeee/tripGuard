package com.tencent.news.core.platform

import com.tencent.news.core.comment.model.IComment
import com.tencent.news.core.list.model.IItemLabel
import com.tencent.news.core.list.model.IKmmFeedsItem
import com.tencent.news.core.list.model.IKmmHotEvent
import com.tencent.news.core.list.model.IKmmNewsModule
import com.tencent.news.core.list.model.IKmmVideoChannel
import com.tencent.news.core.live.model.IKmmNewsRoomInfoData
import com.tencent.news.core.qa.model.IKmmQAInfo
import com.tencent.news.core.tag.model.IKmmTagInfo
import com.tencent.news.core.user.model.IUserInfo
import com.tencent.news.core.video.model.IVideoInfo
import com.tencent.news.qnchannel.api.IChannelInfo
import kotlinx.serialization.json.JsonElement


object QnKmmModelConvert : IPlatformInject {

    var channelInfoParser: IQnKmmModelParser<IChannelInfo>? = null

    var itemParser: IQnKmmModelParser<IKmmFeedsItem>? = null

    var hotEventParser: IQnKmmModelParser<IKmmHotEvent>? = null

    var guestInfoParser: IQnKmmModelParser<IUserInfo>? = null

    var tagInfoParser: IQnKmmModelParser<IKmmTagInfo>? = null // kmm改造完毕，宿主引用待清理

    var newsModuleParser: IQnKmmModelParser<IKmmNewsModule>? = null

    var videoChannelParser: IQnKmmModelParser<IKmmVideoChannel>? = null // kmm改造完毕，宿主引用待清理

    var videoInfoParser: IQnKmmModelParser<IVideoInfo>? = null // kmm改造完毕，宿主引用待清理

    var commentParser: IQnKmmModelParser<IComment>? = null

    var itemLabelParser: IQnKmmModelParser<IItemLabel>? = null

    var newsLiveInfoParser: IQnKmmModelParser<IKmmNewsRoomInfoData>? = null  // kmm改造完毕，宿主引用待清理

    var qaInfoParser: IQnKmmModelParser<IKmmQAInfo>? = null
}

interface IQnKmmModelParser<T> {

    // 使用这个解析，节约一次转换原始json字符串的耗时
    // （但是要实现Gson能用kotlin的JsonElement进行解析，需改造Gson源码）
    fun decodeFromJson(json: JsonElement): T? = null

    fun decodeFromJson(json: String, existModel: Any? = null): T?

    fun encodeToJson(model: T): String

}

// 使用KtJson解析：qnParser迁移完毕的，改为组合方式：拦截Gson的解析派发到KtJson里
// 和 IQnKmmModelParser 基本一样，专门抽取一个子类标记一下哪些类切换完毕了
interface IQnKtCombineModelParser<T> : IQnKmmModelParser<T> {
    override fun decodeFromJson(json: JsonElement): T
    override fun decodeFromJson(json: String, existModel: Any?): T
    override fun encodeToJson(model: T): String
}

fun String?.isEmptyJsonObj(): Boolean = (isNullOrBlank() || this == "{}")