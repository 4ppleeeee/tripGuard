package com.tencent.kmm.demo.startup.sdk.tasks

import android.content.Intent
import android.util.Log
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler

/**
 * Android QQ 登录回调占位。
 */
object AndroidQQLoginRuntime {

    private const val TAG = "AndroidQQLoginRuntime"

    @Volatile
    private var qqAppId: String = ""

    internal fun initialize(appId: String) {
        qqAppId = appId
    }

    fun getAppId(): String = qqAppId

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Boolean {
        Log.i(TAG, "handleActivityResult no-op: requestCode=$requestCode, resultCode=$resultCode")
        return false
    }
}

/**
 * Android 微信登录回调路由。
 *
 * - WXEntryActivity 通过 handleIntent 转发；
 * - 业务登录模块可注册 IWXAPIEventHandler 接收 onResp/onReq。
 */
object AndroidWXLoginRuntime {

    private const val TAG = "AndroidWXLoginRuntime"

    @Volatile
    private var wxAppId: String = ""

    @Volatile
    private var wxApi: IWXAPI? = null

    @Volatile
    private var eventHandler: IWXAPIEventHandler? = null

    private val dispatchHandler = object : IWXAPIEventHandler {
        override fun onReq(req: BaseReq?) {
            eventHandler?.onReq(req)
        }

        override fun onResp(resp: BaseResp?) {
            eventHandler?.onResp(resp)
        }
    }

    internal fun initialize(appId: String, api: IWXAPI) {
        wxAppId = appId
        wxApi = api
    }

    fun registerEventHandler(handler: IWXAPIEventHandler?) {
        eventHandler = handler
    }

    fun getAppId(): String = wxAppId

    fun getWxApi(): IWXAPI? = wxApi

    fun handleIntent(intent: Intent, handler: IWXAPIEventHandler = dispatchHandler): Boolean {
        val api = wxApi ?: return false
        return try {
            api.handleIntent(intent, handler)
        } catch (error: Throwable) {
            Log.e(TAG, "handleIntent failed", error)
            false
        }
    }

    fun dispatchOnReq(req: BaseReq?) {
        eventHandler?.onReq(req)
    }

    fun dispatchOnResp(resp: BaseResp?) {
        eventHandler?.onResp(resp)
    }
}
