package com.tencent.news.qnchannel.api

import com.tencent.news.core.channel.model.KmmChannelInfo
import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.extension.safeEncode
import com.tencent.news.core.list.model.ChannelShowType
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnCompatSerializer
import com.tencent.news.core.list.model.new
import com.tencent.news.core.platform.QnKmmModelConvert
import com.tencent.news.core.serializer.KtJson


/**
 * 大圣系统，频道数据抽象接口；
 * 协议地址：https://git.woa.com/QQNewsOpen/QNProtocol/blob/master/API/Base/ChannelInfo.md
 */
interface IChannelInfo : IChannelState, IKmmKeep {

    // 客户端主动绑定到场景信息
    val env: IChannelEnv

    /**
     * 频道的唯一标识id（普遍是频道的 cmsId）
     */
    var channelKey: String

    /**
     * 频道外显的汉字名称
     */
    var channelName: String?

    /**
     * 频道外显样式（主要影响fragment创建）
     */
    var channelShowType: Int

    val channelWebUrl: String?

    val subChannels: List<IChannelInfo>?

    val redDot: IRedDotInfo?

    val userData: IUserChannelData?

    val barIcon: IIconStyle?

    val city: ICityInfo?

    val entityInfo: IEntityInfo?

    /**
     * 频道支持的客户端最低版本号（低于该版本，频道无法显示）
     */
    val minVersion: Int

    @get:ChannelStatus
    val channelStatus: Int

    val refreshWord: String?

    val extraInfo: MutableMap<String, String>?

    var sub_title: String?
    var title_color_light: String?
    var title_color_light_active: String?
    var title_color_night: String?
    var title_color_night_active: String?
    var desc_color_light: String?
    var desc_color_light_active: String?
    var desc_color_night: String?
    var desc_color_night_active: String?
    var icon: String?
    var icon_active: String?

    val adCode: Int

    object QnSerializer : QnCompatSerializer<IChannelInfo>(
        qnParser = { QnKmmModelConvert.channelInfoParser },
        kmmSerializer = { KmmChannelInfo.serializer() }
    )

    companion object : IQnInterfaceCreator<IChannelInfo> {
        override fun defaultSerializer() = QnSerializer

        fun createDefault(channelKey: String): IChannelInfo = IChannelInfo.new {
            this.channelKey = channelKey
            this.channelShowType = ChannelShowType.COMMON
        }

        fun safeDecode(json: String): IChannelInfo? = KtJson.safeDecode(QnSerializer, json)
        fun safeEncode(data: IChannelInfo): String = KtJson.safeEncode(QnSerializer, data)
    }

}

interface IChannelInfoProvider {
    val channelInfo: IChannelInfo?
}