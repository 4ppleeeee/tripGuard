@file:Suppress("PropertyName")

package com.tencent.news.core.channel.model

import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.list.model.BaseKmmModel
import com.tencent.news.core.list.model.ChannelShowType
import com.tencent.news.core.list.model.new
import com.tencent.news.qnchannel.api.ChannelEnv
import com.tencent.news.qnchannel.api.ChannelState
import com.tencent.news.qnchannel.api.ChannelStatus
import com.tencent.news.qnchannel.api.IChannelEnv
import com.tencent.news.qnchannel.api.IChannelInfo
import com.tencent.news.qnchannel.api.ICityInfo
import com.tencent.news.qnchannel.api.IEntityInfo
import com.tencent.news.qnchannel.api.IIconStyle
import com.tencent.news.qnchannel.api.IRedDotInfo
import com.tencent.news.qnchannel.api.IUserChannelData
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Suppress("AnnotationOnSeparateLine")
typealias QnKmmChannelInfo = @Serializable(IChannelInfo.QnSerializer::class) IChannelInfo

@Serializable
abstract class KmmBaseChannelInfo : BaseKmmModel() {

    @kotlin.jvm.JvmField
    var channel_id: String? = null

    @kotlin.jvm.JvmField
    var channel_name: String? = null

    @kotlin.jvm.JvmField
    var show_type = 0

    internal var channel_entity_id: String? = null
    internal var channel_group_id: String? = null
    internal var channel_group_type = 0
    internal var show_order = 0

    // 地方站相关
    internal var label: String? = null
    internal var type = 0
    internal var group: String? = null
    internal var adcode = 0
    internal var order = 0

    internal var web_url: String? = null

    @ChannelState
    internal var state = 0

    @ChannelStatus
    internal var channel_status = 0

    internal var refresh_word: String? = null

    internal var min_version = 0

    var source: String? = null // debug信息，标识命中的白名单/实验桶 等

    internal var ext_info: MutableMap<String, String>? = null // 自定义属性

    // --story=883305157 【二级频道】替换频道提示用户 协议
//    var action: String? = null
//    var replaced_id: String? = null
//    var action_id: String? = null
//    var actions: List<KmmChannelInfo>? = null


    // todo 复杂数据结构后续再迁移

//    var ext_info: HashMap<String, String>? = null // 自定义属性
//    var sub_channels: List<KmmChannelInfo>? = null
//    var bar_icon: IconStyle? = null
//    var red_dot: RedDotInfo? = null
//    var user_data: UserChannelData? = null
}

@Serializable
open class KmmChannelInfo : KmmBaseChannelInfo(), IChannelInfo {

    override var sub_title: String? = null
    override var title_color_light: String? = null
    override var title_color_light_active: String? = null
    override var title_color_night: String? = null
    override var title_color_night_active: String? = null
    override var desc_color_light: String? = null
    override var desc_color_light_active: String? = null
    override var desc_color_night: String? = null
    override var desc_color_night_active: String? = null
    override var icon: String? = null
    override var icon_active: String? = null

    @Transient
    @kotlin.jvm.Transient
    private var _env: IChannelEnv? = null
    override val env: IChannelEnv
        get() {
            val result = _env ?: ChannelEnv { this }
            _env = result
            return result
        }

    override var channelKey: String
        get() = channel_id.getNonNull()
        set(value) {
            channel_id = value
        }

    override var channelName: String?
        get() = channel_name.getNonNull()
        set(value) {
            channel_name = value
        }

    override var channelShowType: Int
        get() = show_type
        set(value) {
            show_type = value
        }

    override val channelWebUrl: String
        get() = web_url.getNonNull()

    override val minVersion: Int
        get() = min_version

    override val channelStatus: Int
        get() = channel_status

    override val refreshWord: String
        get() = refresh_word.getNonNull()

    override val adCode: Int
        get() = adcode

    override val channelState: Int
        get() = state


    override val extraInfo: MutableMap<String, String>
        get() {
            val result = ext_info ?: mutableMapOf()
            ext_info = result
            return result
        }

    override val subChannels: List<IChannelInfo>?
        get() = null

    override val redDot: IRedDotInfo?
        get() = null

    override val userData: IUserChannelData?
        get() = null

    override val barIcon: IIconStyle?
        get() = null


    @Transient
    @kotlin.jvm.Transient
    private var _entityInfo: IEntityInfo? = null
    override val entityInfo: IEntityInfo
        get() {
            val result = _entityInfo ?: KmmChannelEntityInfo(this)
            _entityInfo = result
            return result
        }

    @Transient
    @kotlin.jvm.Transient
    private var _city: ICityInfo? = null
    override val city: ICityInfo
        get() {
            val result = _city ?: KmmChannelCityInfo(this)
            _city = result
            return result
        }

    companion object {
        fun createQnInstance(
            channelKey: String,     // @NewsChannel
            channelName: String,
            channelShowType: Int = ChannelShowType.COMMON_LIST,
        ): IChannelInfo {
            return IChannelInfo.new {
                this.channelKey = channelKey
                this.channelName = channelName
                this.channelShowType = channelShowType
            }
        }

        fun createDefault(): IChannelInfo {
            return createQnInstance(
                channelKey = "all",
                channelName = "综合"
            )
        }
    }

}

class KmmChannelEntityInfo(private val channel: KmmBaseChannelInfo) : IEntityInfo {

    override var entityId: String? by channel::channel_entity_id

    override val entityGroup: String?
        get() = channel.channel_group_id

    override val groupType: Int
        get() = channel.channel_group_type

    override val showOrder: Int
        get() = channel.show_order

}

class KmmChannelCityInfo(private val channel: KmmBaseChannelInfo) : ICityInfo {

    override val label: String
        get() = channel.label.getNonNull()

    override val channelType: Int
        get() = channel.type

    override val chanelGroup: String
        get() = channel.group.getNonNull()

    override val adCode: Int
        get() = channel.adcode

    override val order: Int
        get() = channel.order

}
