package com.tencent.news.qnchannel.api

interface IChannelConfig {
    /**
     * 非个性化下，合并内置与大圣下发数据
     * @param defaultConfig 内置数据
     */
    fun mergePersonalizedConfig(defaultConfig: IChannelConfig?)

    /**
     * 业务侧-网络结果返回码，非0值均为异常
     */
    val retCode: Int

    /**
     * 用户导航版本号
     */
    val version: Int

    /**
     * config 级别基础数据校验（该方法不用对内容校验太严，后续在parser里会校验每个tab内容）
     */
    val isDataInvalid: Boolean

    /**
     * 用户导航列表
     */
    val userChannels: List<String>?

    /**
     * 用户导航列表，从 6260开始扩展为 [IChannelInfo] 结构
     */
    val userChannelsInfo: List<IChannelInfo>?

    val normalChannelsGroup: IChannelGroup?

    /**
     * 获取tab集合数据
     *
     * @param tabId 页卡id（各tab取值为客户端固定写死，不随后台下发变动）
     */
    fun getChannelGroup(@ChannelTabId tabId: String): IChannelGroup?

    /**
     * 推荐的地方站（目前取数组0位代表推荐的地方站）
     */

    val recommendCity: List<IChannelInfo>?


    fun getChannelLikeRadioInfo(): IChannelGroup

    /**
     * 是否是由于数据上传，返回的本次config
     */
    fun triggerByUpload(): Boolean

    fun setTriggerByUpload()

    /**
     * 通用开关配置
     */
    val configMap: Map<String, String>?

    /**
     * 大圣系统返回的城市定位编码
     */
    val adCode: Int

    val recommendAction: List<IChannelInfo>?

    companion object {
        const val RET_OK = 0
        const val RET_SIGN_FAILED = -2
    }

}