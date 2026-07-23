@file:Suppress("PropertyName", "PrivatePropertyName", "unused")

package com.tencent.news.core.tads.constants

import com.tencent.news.core.extension.IKmmPure
import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.extension.isTrue
import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.isAndroidPlatform
import com.tencent.news.core.isHarmonyPlatform
import com.tencent.news.core.list.model.BaseKmmModel
import com.tencent.news.core.platform.api.getShiplySwitch
import com.tencent.news.core.serializer.KtJson
import com.tencent.news.core.service.FrameworkServiceBridge
import com.tencent.news.core.tads.model.AdFormComponentContent
import com.tencent.news.core.tads.model.AdFormComponentContentSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


object AdJumpActionType {
    const val UNKNOWN = 0               // 未知
    const val WEB = 1                   // WebView h5类跳转
    const val JUMP_DIRECTLY = 2         // 直达类包括dp, ul
    const val WX_PROGRAM = 3            // 小程序,小游戏类
    const val DOWNLOAD = 4              // 下载类
}

/*
延迟双开：因为安卓/鸿蒙在打开外链时，会有系统弹窗拦截，所以需要有弹窗拦截行为判断。
流程描述：
action_0 -> action_1（延迟双开）执行action_1前，开始进行计时等待，最终有三种结果
1. **timeOut（超时）**：
   = false
2. **stop（停止）**：
     - 如果时间间隔 **大于1.5秒**，进入 **notShow(弹窗未展现)** 节点；
     - 如果时间间隔 **小于1.5秒**，进入 **confirm** 节点。
   = false。
3. **cancel（取消）**：
   = true。

true -> 代表在action_0用户明确取消了打开弹窗，才能执行下一个节点action_1。
false -> 终止action执行链
*/
object AdJumpActionNext {
    const val WHEN_FAILED = 0                           // 当前失败后再执行下一节点
    const val IMMEDIATELY = 1                           // 立即执行下一个节点(双开)
    const val WHEN_SUCCEED = 2                          // 当前节点成功后执行下一节点
    const val AFTER_PAGE_VISIBLE = 3                    // 当前节点页面可见后再执行下—个节点，依赖详情页展示行为
    const val AFTER_SYS_BLOCK = 4                       // 延迟双开，通过系统弹窗及用户发态判断状态，失败后执行下一节点
    const val AFTER_FAIL_AND_APP_UNINSTALLED = 5        // 当前节点失败且app未安装时执行下一节点
}


object AdWxLinkType {
    const val PROGRAM = 1               // 微信小程序
    const val GAME = 2                  // 微信小游戏
    const val NATIVE = 3                // 微信原生页
    const val STORE = 5                 // 微信小店

    fun isValidType(type: Int): Boolean {
        return type in listOf(PROGRAM, GAME, NATIVE, STORE)
    }
}


object AdDeepLinkType {
    const val UNKNOWN = 0         // 未知
    const val SCHEME = 1          // 对应scheme(普通应用直达)
    const val UNIVERSAL_LINK = 2  // 对应universal_link(ios)
    const val HAP = 3             // 对应landing_page_info.quick_jump_info.hap_jump_scheme
    const val MARKET_DOWNLOAD = 4 // 对应landing_page_info.jump_android_market_info.market_deep_link

    fun isValidType(type: Int): Boolean {
        return type in listOf(SCHEME, UNIVERSAL_LINK, HAP, MARKET_DOWNLOAD)
    }
}

object AdWebJumpType {
    const val UNKNOWN = 0              // 未知
    const val H5 = 1                   // H5
    const val COMPONENT_WIDGET = 2     // 创意组件
    const val NATIVE_LANDING_PAGE = 4  // 原生落地页
}

object AdComponentType {
    const val UNKNOWN = 0      // 未知
    const val PHONE = 1        // 电话组件
    const val FORM = 2         // 外显表单
    const val CONSULT = 3      // 咨询组件
}

object AdJumpLinkClickArea {
    const val DEFAULT = 0         // 默认
    const val ACTION_BUTTON = 1   // 行动按钮按钮
}

object AdTriggerType {
    const val DEFAULT = 0       // 默认点击
}


@Serializable
class AdJumpAction : BaseKmmModel(), IKmmPure {
    @SerialName("jump_ability_id")
    var jumpAbilityId: Int = 0

    var next = 0

    @SerialName("action_type")
    val actionType: Int = 0

    @SerialName("web_data")
    val webData: AdWebData? = null

    @SerialName("deep_link_data")
    val deepLinkData: AdDeepLinkData? = null

    @SerialName("wx_link_data")
    val wxLinkData: AdWxLinkData? = null

    @SerialName("download_data")
    val downloadData: AdDownloadData? = null


    fun isDataValid(): Boolean {
        return when (actionType) {
            AdJumpActionType.JUMP_DIRECTLY -> deepLinkData?.isDataValid().isTrue()
            AdJumpActionType.WX_PROGRAM -> wxLinkData?.isDataValid().isTrue()
            else -> webData?.isDataValid().isTrue()
        }
    }

    fun isActionValid(): Boolean {
        return when (actionType) {
            AdJumpActionType.JUMP_DIRECTLY -> deepLinkData?.isActionValid().isTrue()
            AdJumpActionType.WX_PROGRAM -> wxLinkData?.isActionValid().isTrue()
            else -> webData?.isActionValid().isTrue()
        }
    }

    companion object {
        fun safeDecodeList(json: String): List<AdJumpAction>? =
            KtJson.safeDecode<List<AdJumpAction>>(json)
    }

}


@Serializable
class AdWebData : IAdJumpActionData, BaseKmmModel(), IKmmPure {
    var url: String? = null // 真实跳转的url（对应dest_url）

    val type: Int = 0

    @SerialName("ext_info")
    var extInfo: AdJumpExtInfo? = null

    override fun isDataValid(): Boolean {
        return when (type) {
            AdWebJumpType.H5, AdWebJumpType.UNKNOWN ->
                url.isNotNullOrEmpty() || extInfo?.clickUrl.isNotNullOrEmpty()

            AdWebJumpType.COMPONENT_WIDGET ->
                extInfo?.component_content != null

            AdWebJumpType.NATIVE_LANDING_PAGE -> true // todo torreszhang opt: 这里应该校验下模板id
            else -> true
        }
    }


    override fun isActionValid(): Boolean = isDataValid() // web暂无其他校验，数据合法即可跳转

}


@Serializable
class AdDeepLinkData : IAdJumpActionData, BaseKmmModel(), IKmmPure {

    var url: String? = null

    var type = AdDeepLinkType.UNKNOWN

    @SerialName("ext_info")
    var extInfo: AdJumpExtInfo? = null

    override fun isDataValid(): Boolean {

        if (!AdDeepLinkType.isValidType(type)) return false

        return when (type) {
            AdDeepLinkType.SCHEME -> url.isNotNullOrEmpty()
            AdDeepLinkType.UNIVERSAL_LINK -> url.isNotNullOrEmpty()
            else -> true
        }
    }


    override fun isActionValid(): Boolean {
        if (!isDataValid()) {
            return false
        }
        if (type == AdDeepLinkType.UNIVERSAL_LINK) {
            return true // iOS的ulink无法明确判定app是否安装，都当做合法跳转
        }
        return adAppChecker().isAppInstalled(url, "")
    }

    override fun isDirectJumpAndAppUnInstalled(): Boolean {
        if (type == AdDeepLinkType.UNIVERSAL_LINK || url.isNullOrEmpty()) {
            return false // iOS的ulink无法明确判定app是否安装，都当做合法跳转
        }
        return !adAppChecker().isAppInstalled(url, "")
    }

}


@Serializable
class AdWxLinkData : IAdJumpActionData, BaseKmmModel(), IKmmPure {
    var url: String? = null         // mini_program_info.path
    var type = 0                    // @AdWxLinkType

    @SerialName("ext_info")
    var extInfo: AdJumpExtInfo? = null

    override fun isDataValid(): Boolean {

        if (!AdWxLinkType.isValidType(type)) return false

        return when (type) {
            AdWxLinkType.STORE -> url.isNotNullOrEmpty()
            AdWxLinkType.NATIVE -> extInfo?.canvasExt.isNotNullOrEmpty()
            else -> extInfo?.username.isNotNullOrEmpty() && url.isNotNullOrEmpty()
        }
    }

    override fun isActionValid(): Boolean = isDataValid() && adAppChecker().isWxAppInstalled()

}


@Serializable
class AdDownloadData : IAdJumpActionData, BaseKmmModel(), IKmmPure {
    var url: String? = null

    override fun isDataValid(): Boolean {
        return super.isDataValid()
    }

    override fun isActionValid(): Boolean {
        return super.isActionValid()
    }
}


@Serializable
class AdJumpExtInfo : BaseKmmModel(), IKmmPure {
    @SerialName("qz_gdt")
    var qzGdt: String = ""                 // 对应click_id，端上暂时无用

    @SerialName("click_url")
    var clickUrl: String = ""              // 带点击计费上报的url，支持换链

    /**
     * type=TYPE_DEEP_LINK， 对应app_name
     * type=TYPE_UNIVERSAL_LINK
     * type=TYPE_HAP， 对应landing_page_info.quick_jump_info.hap_name
     * type=TYPE_MARKET_DOWNLOAD
     */
    @SerialName("app_name")
    var appName: String = ""
    var username: String = ""               // mini_program_info.username
    var token: String = ""                  // mini_program_info.token

    @SerialName("appid")
    var appId: String = ""                  // mini_program_info.wx_appid

    @SerialName("trace_data")
    var traceData: String = ""             // mini_program_info.ad_trace_data

    @SerialName("canvas_ext")
    var canvasExt: String = ""             // wechat_extinfo 7100不下发

    @SerialName("ios_wx_appid")     // 区别于wechatAppId，用于jump_actions逻辑里兜底打开微信appstore页
    val wxIOSAppId: String = ""

    @SerialName("author_name")
    var authorName: String = ""

    @SerialName("package_size_bytes")
    var packageSizeBytes: Long = 0

    @SerialName("version_name")
    var versionName: String = ""

    @SerialName("permissions_url")
    var permissionsUrl: String = ""

    @SerialName("privacy_agreement_url")
    var privacyAgreementUrl: String = ""

    /**
     * type=TYPE_DEEP_LINK， 对应jd_package_name (只有京东应用直达，Android用于判断用户是否安装广告主APP，已用不到)
     * type=TYPE_UNIVERSAL_LINK,  京东商品类型pt25下对应 jd_package_name （客户端用不到）
     * type=TYPE_HAP， landing_page_info.quick_jump_info.hap_package_name
     * type=TYPE_MARKET_DOWNLOAD,  对应landing_page_info.jump_android_market_info.market_package_name
     */
    @SerialName("package_name")
    var packageName: ArrayList<String>? = null

    @SerialName("reward_landing_page_url")
    var rewardUrl: String? = null  // 激励型落地页面


    /**
     * 创意组件类型
     */
    @SerialName("component_type")
    var componentType: Int = 0

    /**
     * 创意组件数据
     */
    @Serializable(with = AdFormComponentContentSerializer::class)
    var component_content: AdFormComponentContent? = null    // 组件更多信息的json
}


@Serializable
class AdJumpLinkMap : BaseKmmModel(), IKmmPure {

    @SerialName("jump_link_info_graph")
    val jumpLinkInfoGraph: List<AdJumpLinkInfoGraph> = emptyList()

}

@Serializable
class AdJumpLinkInfoGraph : BaseKmmModel(), IKmmPure {

    @SerialName("trigger_condition")
    val condition: AdJumLinkInfoCondition? = null

    @SerialName("jump_graph")
    val jumpAction: List<AdGraphJumpAction> = emptyList()
}

@Serializable
class AdJumLinkInfoCondition : BaseKmmModel(), IKmmPure {
    @SerialName("click_area")
    var clickArea: Int = 0          // @AdJumpLinkClickArea

    @SerialName("trigger_type")
    val triggerType: Int = 0        // @AdTriggerType
}

@Serializable
class AdJumpNextNode : BaseKmmModel(), IKmmPure {

    @SerialName("next_action")
    val nextAction: Int = 0     // 下一节点的执行条件,对应枚举@AdJumpActionNext

    @SerialName("node_index")
    val nodeIndex: Int = INVALID_NUM   // 指向下一节点的索引 @AdJumpLinkInfoGraph
}


@Serializable
data class AdGraphJumpAction(
    @SerialName("jump_ability_id")
    val jumpAbilityId: Int = 0,

    // @AdJumpActionType
    @SerialName("action_type")
    val actionType: Int = 0,

    // 标识从jumpAction数组第index位置取值
    @SerialName("data_index")
    val dataIndex: Int = INVALID_NUM,

    // ［graph索引使用］下一跳节点列表，支持单分支或多分支
    @SerialName("next_node_list")
    val nextNodeList: List<AdJumpNextNode> = emptyList(),

    // @AdJumpActionNext
    val next: Int = 0
) : BaseKmmModel(), IKmmPure {
    fun isDirectJumpAndUseSubGraph(): Boolean {
        if (!getShiplySwitch("enable_v2_jumpGraph_nodeList", true)) {
            return false
        }
        return (isAndroidPlatform()|| isHarmonyPlatform())
                && actionType == AdJumpActionType.JUMP_DIRECTLY
                && nextNodeList.isNotNullOrEmpty()
    }
}

@Serializable
class AdJumpLinkInfoData : BaseKmmModel(), IKmmPure {
    @SerialName("web_data")
    val webData: AdWebData? = null

    @SerialName("deep_link_data")
    val deepLinkData: AdDeepLinkData? = null

    @SerialName("wx_link_data")
    val wxLinkData: AdWxLinkData? = null

    @SerialName("download_data")
    val downloadData: AdDownloadData? = null
}

interface IAdJumpActionData {

    // 判断当前跳转Action数据合法性
    fun isDataValid(): Boolean = false

    // 判断当前跳转Action合法性
    fun isActionValid(): Boolean = false

    // 判断当前链接是app跳转并且app未安装
    fun isDirectJumpAndAppUnInstalled(): Boolean = false
}

private fun adAppChecker() = FrameworkServiceBridge.impl.getAppChecker()
