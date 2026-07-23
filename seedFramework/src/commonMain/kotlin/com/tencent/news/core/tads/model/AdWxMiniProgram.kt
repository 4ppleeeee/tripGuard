package com.tencent.news.core.tads.model

import com.tencent.news.core.list.model.BaseKmmModel
import kotlinx.serialization.Serializable


interface IWxMiniProgram {

    var userName: String

    var path: String

    var token: String

    var wxAppId: String

    var adTraceData: String

    val appId: String

    // 6970：【【商业化-广告】【新闻主App】【广告链路】蹊径跳转小游戏支持平台自动归因】
    // https://tapd.woa.com/adnewplatform/prong/stories/view/1010161211876222839
    var invokeData: String // 前端归因用的透传字段，解析的是jsapi传参的 extMsg.invokeData 字段做透传

    // 内开小游戏
    var enableInnerOpen: Boolean
    var needLogin: Boolean
    var oid: String
    var pageType: String

}

@Suppress("PrivatePropertyName")
@Serializable
class AdWxMiniProgram : BaseKmmModel(), IWxMiniProgram {

    private var user_name: String = ""
    override var userName: String
        get() = user_name
        set(value) {
            user_name = value
        }

    override var path: String = ""

    override var token: String = ""

    private var wx_appid: String = ""
    override var wxAppId: String
        get() = wx_appid
        set(value) {
            wx_appid = value
        }

    private var appid: String = ""

    override val appId: String
        get() = appid

    private var ad_trace_data: String = ""
    override var adTraceData: String
        get() = ad_trace_data
        set(value) {
            ad_trace_data = value
        }

    override var invokeData: String = ""

    private var enable_inner_open: Boolean = false
    override var enableInnerOpen: Boolean by ::enable_inner_open

    private var need_login: Boolean = false
    override var needLogin: Boolean by ::need_login

    override var oid: String = ""
    override var pageType: String = ""

}
