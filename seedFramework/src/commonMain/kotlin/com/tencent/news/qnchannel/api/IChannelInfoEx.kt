@file:Suppress("unused")

package com.tencent.news.qnchannel.api

import com.tencent.news.core.channel.config.ChannelConfig
import com.tencent.news.core.channel.constants.NewsChannel
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.extension.getJsonStr
import com.tencent.news.core.extension.getNonNull
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.extension.jsonObj2Map
import com.tencent.news.core.extension.safeToInt
import com.tencent.news.core.extension.toIntMap
import com.tencent.news.core.extension.toJsonElement
import com.tencent.news.core.extension.toJsonObject

/**
 * 获取大圣系统，通用透传参数里配置的列表相关协议；与 IChannelModel.kt 中的方法类似，区别在于：
 * 这里的属性是大圣配置下发的，IChannelModel.kt里是客户端本地赋值的
 */

// 请求的接口名，例如：getQQNewsUnreadList（since：6.9.50）
fun IChannelInfo?.getRequestCgi(): String? = getExtraConfig("request_cgi")

fun IChannelInfo?.setRequestCgi(cgi: String) {
    setExtraConfig("request_cgi", cgi)
}

// 列表使用的缓存类型
fun IChannelInfo?.getCacheType(): Int? = getExtraInt("cacheType")

fun IChannelInfo?.setCacheType(cacheType: Int) = setExtraInt("cacheType", cacheType)

// 频道撑满全屏
fun IChannelInfo?.isFullScreen(): Boolean =
    getExtraInt("full_screen") == 1 || this?.debugFullScreen().isTrue()

private fun IChannelInfo.debugFullScreen(): Boolean =
    ChannelConfig.fullScreenChannelList.contains(channelKey)

// 列表使用的presenter类型
fun IChannelInfo?.getPresenterType(): Int? = getExtraInt("presenterType")

// 列表使用的视频播放逻辑类型
fun IChannelInfo?.getVideoPlayMode(): Int? = getExtraInt("videoPlayMode")

// 品字形框架的header类型（预留，暂未改造完毕）
fun IChannelInfo?.getHeaderType(): Int? = getExtraInt("headerType")

// 品字形框架的导航条类型（预留，暂未改造完毕）
fun IChannelInfo?.getChannelBarType(): Int? = getExtraInt("channelBarType")

// 品字形频道中，需要固定到左侧的频道
fun IChannelInfo?.isFixPosSubChannel(): Boolean = getExtraInt("sub_channel_fix_pos") == 1

fun IChannelInfo?.isForbidTopRefresh(): Boolean = getExtraInt("forbid_top_refresh") == 1

private fun IChannelInfo?.getExtraInt(key: String): Int? {
    return this?.extraInfo?.get(key)?.toFloatOrNull()?.toInt()
}

private fun IChannelInfo?.setExtraInt(key: String, value: Int) {
    this?.extraInfo?.set(key, value.toString())
}

// 是否为tag频道：channel_entity_id不为空
fun IChannelInfo?.hasEntityId(): Boolean = this?.entityInfo?.entityId?.isNotEmpty() ?: false

// reset时间间隔
fun IChannelInfo?.getResetTime(): Int = getExtraInt("reset_time") ?: 0

fun IChannelInfo?.getWidgetAction(): String? = this?.extraInfo?.get("widget_action")

fun IChannelInfo?.forbidStruct(): Boolean = getExtraInt("forbid_struct") == 1

fun IChannelInfo?.setForbidStruct(forbid: Boolean) {
    setExtraConfig("forbid_struct", if (forbid) "1" else "0")
}

fun IChannelInfo?.getModTime(): Long = this?.userData?.modifyTime ?: 0L

fun IChannelInfo?.getExtraConfig(key: String): String? = this?.extraInfo?.get(key)

private fun IChannelInfo?.setExtraConfig(key: String, value: String) {
    this?.extraInfo?.set(key, value)
}

fun IChannelInfo?.isEventChannel(): Boolean = this?.extraInfo?.get("scene") == "channel"

// @since 7080 导航条lottie图标url
fun IChannelInfo?.getIconLottieUrl(): String { // 注意，安卓/iOS 字段不一样，两端都要记得配上
    return getExtraConfig("icon_lottie_url_android") ?: ""
}

// @since 7080 导航条lottie图标宽度（单位dp）
fun IChannelInfo?.getIconLottieWidth(): Int {
    return getExtraInt("icon_lottie_width") ?: 0
}

// @since 7080 限定该频道生效的皮肤包资源id，不配则不限制，正常从shiply获取
fun IChannelInfo?.getChannelSkinMatch(): String {
    return getExtraConfig("skin_match") ?: ""
}

fun IChannelInfo?.getDefaultTab(): String {
    return getExtraConfig("default_tab") ?: ""
}

fun IChannelInfo?.getRequestHost(): String {
    return getExtraConfig("request_host") ?: ""
}

fun IChannelInfo?.getRequestDomain(): String {
    return getExtraConfig("request_domain") ?: ""
}

fun IChannelInfo?.getRequestIp(): String {
    return getExtraConfig("request_ip") ?: ""
}

/**
 * 三级导航是否隐藏"分类排序"
 */
fun IChannelInfo?.hideSubChannelEditBtn(): Boolean {
    return 1 == getExtraInt("hide_sub_channel_edit_button")
}

/**
 * 三级导航是否隐藏下拉展开
 */
fun IChannelInfo?.hideSubChannelExpandBtn(): Boolean {
    return 1 == getExtraInt("hide_sub_channel_expand_button")
}

/**
 * 三级导航主频道外显名（默认为"综合"）
 */
fun IChannelInfo?.getSubChannelMainName(): String {
    return getExtraConfig("sub_channel_main_name") ?: ""
}

/**
 * 频道日间背景色值 0x123456
 */
fun IChannelInfo?.getBgColorDay(defaultColor: Int): Int {
    return getHexColorInt("bgColorDay", defaultColor)
}

/**
 * 频道夜间背景色值 0x123456
 */
fun IChannelInfo?.getBgColorNight(defaultColor: Int): Int {
    return getHexColorInt("bgColorNight", defaultColor)
}

/**
 * 频道右上方操作icon日间色值 0x123456
 */
fun IChannelInfo?.getIconColorDay(defaultColor: Int): Int {
    return getHexColorInt("iconColorDay", defaultColor)
}

/**
 * 频道右上方操作icon夜间色值 0x123456
 */
fun IChannelInfo?.getIconColorNight(defaultColor: Int): Int {
    return getHexColorInt("iconColorNight", defaultColor)
}

fun IChannelInfo?.getDesc(): String {
    return this.getExtraConfig("desc") ?: ""
}

fun IChannelInfo?.getEntityParams(): Map<String, String> {
    val entity = this?.entityInfo
        ?: return emptyMap()

    return mapOf(
        "channel_entity_id" to entity.entityId.getNonNull(),
        "channel_group_id" to entity.entityGroup.getNonNull(),
        "channel_group_type" to entity.groupType.toString(),
    ).filter { it.value.isNotNullOrEmpty() }
}

/**
 * 0x123456 -> FF123456
 * 0xfa123456 -> fa123456
 */
private fun IChannelInfo?.getHexColorInt(key: String, defaultColor: Int): Int {
    var value = getExtraConfig(key) ?: return defaultColor
    if (value.startsWith("0x", ignoreCase = true)) {
        value = value.substring(2)
        if (value.length == 6) {
            value = "FF$value"
        }

        if (value.length == 8) {
            kotlin.runCatching {
                return value.toLong(16).toInt()
            }.getOrDefault(defaultColor)
        }
    }

    return defaultColor
}

fun IChannelInfo?.isIntegrationTestChannel(): Boolean {
    return getExtraConfig("is_integration_test") == "1"
}

fun IChannelInfo?.isUseGlobalCache(): Boolean {
    return getExtraConfig("global_cache") == "1"
}

fun IChannelInfo?.setEnableGlobalCache(enable: Boolean) {
    this?.extraInfo?.set("global_cache", if (enable) "1" else "0")
}

fun IChannelInfo?.logKey(): String {
    return this?.channelKey ?: ""
}

fun IChannelInfo?.setGridLayoutManagerConfig(spanSize: Int, mapOf: Map<Int, Int>) {
    this?.extraInfo?.set("gridSpanSize", "$spanSize")
    this?.extraInfo?.set("gridSpanConfig", mapOf.toJsonElement().getJsonStr())
}

fun IChannelInfo?.getGridLayoutSize(): Int {
    return getExtraConfig("gridSpanSize").safeToInt(0)
}

fun IChannelInfo?.getGridSpanConfig(): Map<String, Int>? {
    return getExtraConfig("gridSpanConfig").toJsonObject()?.jsonObj2Map()?.toIntMap()
}

fun IChannelInfo?.enableGridLayoutManager(): Boolean {
    return getGridLayoutSize() > 0
}

// 简化的地方站判断逻辑：有下发城市区位码就是地方站（就不用从集合里查数据了）
fun IChannelInfo?.isLocalChannel(): Boolean {
    val adCode = this?.city?.adCode ?: return false
    return adCode > 0
}

/**
 * 是724相关频道吗
 */
fun IChannelInfo?.is724RelateChannel() =
    this?.channelKey?.startsWith(NewsChannel.NEWS_NEWS_724) == true

/**
 * 是724垂类频道吗
 */
fun IChannelInfo?.isVertical724Channel() =
    this?.channelKey?.startsWith("${NewsChannel.NEWS_NEWS_724}_") == true

// @ComposePageType
fun IChannelInfo?.getComposePageType(): String? = getExtraConfig("compose_page_type")

fun IChannelInfo.updatePageArgs(pageArgs: IComposePageArgs?, rebuild: Boolean = true) {
    env.pageArgs = pageArgs
    if (rebuild) {
        env.rebuildStatusKey = pageArgs?.identifier?.toString()
    }
}