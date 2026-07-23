package com.tencent.kmm.demo.wxapi

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.kmm.demo.startup.sdk.tasks.AndroidWXLoginRuntime

/**
 * 微信登录回调页。
 *
 * 由微信 SDK 回调拉起本 Activity，并在此分发给业务登录层。
 */
class WXEntryActivity : FragmentActivity(), IWXAPIEventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)
        handleWxIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        overridePendingTransition(0, 0)
        super.onNewIntent(intent)
        setIntent(intent)
        handleWxIntent(intent)
    }

    private fun handleWxIntent(intent: Intent?) {
        if (intent == null) {
            finishWithoutAnimation()
            return
        }
        AndroidWXLoginRuntime.handleIntent(intent, this)
        finishWithoutAnimation()
    }

    override fun onReq(req: BaseReq?) {
        AndroidWXLoginRuntime.dispatchOnReq(req)
        finishWithoutAnimation()
    }

    override fun onResp(resp: BaseResp?) {
        AndroidWXLoginRuntime.dispatchOnResp(resp)
        finishWithoutAnimation()
    }

    private fun finishWithoutAnimation() {
        if (!isFinishing) {
            finish()
        }
        overridePendingTransition(0, 0)
    }
}
