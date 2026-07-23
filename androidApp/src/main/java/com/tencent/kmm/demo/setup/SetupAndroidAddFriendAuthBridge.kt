package com.tencent.kmm.demo.setup

import android.app.Activity
import com.tencent.connect.auth.AuthAgent
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.getRealContext
import com.tencent.news.core.platform.api.appLogin
import com.tencent.tauth.IUiListener
import com.tencent.tauth.UiError
import com.tencent.kmm.startup.std.trace.DbgFriendAuthLog
import com.tencent.kmm.startup.std.trace.UserLog
import com.tencent.kmm.demo.core.user.addfriend.api.AddFriendChainAuthBridge
import com.tencent.kmm.demo.core.user.addfriend.api.AddFriendChainAuthBridgeRegistry
import com.tencent.kmm.demo.core.user.addfriend.api.QQ_FRIEND_CHAIN_AUTH_SCOPE
import com.tencent.kmm.demo.core.user.addfriend.model.ADD_FRIEND_AUTH_TYPE_QQ
import com.tencent.kmm.demo.core.user.addfriend.model.ADD_FRIEND_AUTH_TYPE_WECHAT
import com.tencent.kmm.demo.core.user.addfriend.model.AddFriendAuthAccountMismatchException
import com.tencent.kmm.demo.core.user.addfriend.model.AddFriendChainAuthCanceledException
import com.tencent.kmm.demo.core.user.addfriend.model.AddFriendChainAuthRequest
import com.tencent.kmm.demo.startup.sdk.tasks.AndroidQQLoginRuntime
import com.tencent.kmm.demo.startup.sdk.tasks.AndroidWXLoginRuntime
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject

internal fun setupAndroidAddFriendAuthBridge() {
    DbgFriendAuthLog.fileLog("Setup", "register AndroidAddFriendChainAuthBridge")
    UserLog.fileLog("AddFriendAuthBridge", "register AndroidAddFriendChainAuthBridge")
    AddFriendChainAuthBridgeRegistry.register(AndroidAddFriendChainAuthBridge)
}

private object AndroidAddFriendChainAuthBridge : AddFriendChainAuthBridge {
    override suspend fun requestChainAuth(authType: Int): Result<AddFriendChainAuthRequest> {
        if (authType == ADD_FRIEND_AUTH_TYPE_QQ) {
            DbgFriendAuthLog.fileLog("Sdk", "request api=requestChainAuth authType=$authType")
        }
        UserLog.fileLog(TAG, "requestChainAuth dispatch authType=$authType")
        UserLog.debug(TAG) { "requestChainAuth dispatch authType=$authType" }
        return when (authType) {
            ADD_FRIEND_AUTH_TYPE_QQ -> requestQQChainAuth()
            ADD_FRIEND_AUTH_TYPE_WECHAT -> requestWechatChainAuth()
            else -> Result.failure(IllegalArgumentException("unsupported authType=$authType").also {
                DbgFriendAuthLog.error("Sdk", "response api=requestChainAuth result=failed authType=$authType error=unsupported", it)
            })
        }
    }

    private suspend fun requestQQChainAuth(): Result<AddFriendChainAuthRequest> {
        val activity = currentActivity()
            ?: return Result.failure(IllegalStateException("No foreground Activity").also {
                DbgFriendAuthLog.error("QQSdk", "start failed: no foreground Activity", it)
                UserLog.error(TAG, "QQ chain auth start failed: no foreground Activity", it)
            })
        val tencent = AndroidQQLoginRuntime.getTencent()
            ?: return Result.failure(IllegalStateException("Tencent SDK is not initialized").also {
                DbgFriendAuthLog.error("QQSdk", "start failed: Tencent SDK is not initialized", it)
                UserLog.error(TAG, "QQ chain auth start failed: Tencent SDK is not initialized", it)
            })
        DbgFriendAuthLog.fileLog(
            "QQSdk",
            "request api=QQ.login activity=${activity::class.simpleName} scope=$QQ_CHAIN_AUTH_SCOPE",
        )
        DbgFriendAuthLog.debug("QQSdk") {
            "request api=QQ.login activity=${activity::class.qualifiedName} scope=$QQ_CHAIN_AUTH_SCOPE"
        }
        UserLog.fileLog(TAG, "QQ chain auth start activity=${activity::class.simpleName}")
        UserLog.debug(TAG) { "QQ chain auth login start scope=$QQ_CHAIN_AUTH_SCOPE activity=${activity::class.qualifiedName}" }
        return suspendCancellableCoroutine { continuation ->
            val listener = object : IUiListener {
                override fun onComplete(response: Any?) {
                    DbgFriendAuthLog.fileLog("QQSdk", "response api=QQ.login callback=onComplete responseType=${response?.let { it::class.simpleName }}")
                    UserLog.fileLog(TAG, "QQ chain auth onComplete responseType=${response?.let { it::class.simpleName }}")
                    val parsedResult = runCatching {
                        val json = response as? JSONObject
                            ?: throw IllegalStateException("QQ auth response is not JSONObject")
                        val openId = json.optString(KEY_OPEN_ID)
                            .ifBlank { json.optString(KEY_OPENID) }
                        val accessToken = json.optString(KEY_ACCESS_TOKEN)
                        UserLog.debug(TAG) {
                            "QQ chain auth client-side credential parsed hasOpenId=${openId.isNotBlank()} tokenLen=${accessToken.length}"
                        }
                        if (openId.isBlank()) {
                            throw IllegalStateException("QQ openId is empty")
                        }
                        if (accessToken.isBlank()) {
                            throw IllegalStateException("QQ accessToken is empty")
                        }
                        DbgFriendAuthLog.fileLog(
                            "QQSdk",
                            "response api=QQ.login parsed hasOpenId=${openId.isNotBlank()} tokenLen=${accessToken.length}",
                        )
                        AddFriendChainAuthRequest(
                            authType = ADD_FRIEND_AUTH_TYPE_QQ,
                            openId = openId,
                            openToken = accessToken,
                            submitToChainBind = shouldSubmitQQChainBind(openId),
                        )
                    }
                    parsedResult.onFailure {
                        DbgFriendAuthLog.error("QQSdk", "response api=QQ.login result=failed stage=parseCredential", it)
                        UserLog.error(TAG, "QQ chain auth parse failed", it)
                        AndroidQQLoginRuntime.clearAuthListener(this)
                        continuation.resume(Result.failure(it))
                        return
                    }
                    val authRequest = parsedResult.getOrThrow()
                    UserLog.fileLog(
                        TAG,
                        "QQ chain auth success hasOpenId=${authRequest.openId.isNotBlank()} " +
                            "tokenLen=${authRequest.openToken.length}",
                    )
                    DbgFriendAuthLog.fileLog(
                        "QQSdk",
                        "response api=requestQQChainAuth result=success hasOpenId=${authRequest.openId.isNotBlank()} " +
                            "tokenLen=${authRequest.openToken.length}",
                    )
                    AndroidQQLoginRuntime.clearAuthListener(this)
                    continuation.resume(Result.success(authRequest))
                }

                override fun onError(error: UiError?) {
                    DbgFriendAuthLog.error(
                        "QQSdk",
                        "response api=QQ.login result=failed callback=onError code=${error?.errorCode}, msg=${error?.errorMessage}, detail=${error?.errorDetail}",
                    )
                    UserLog.error(
                        TAG,
                        "QQ chain auth SDK error code=${error?.errorCode}, msg=${error?.errorMessage}, detail=${error?.errorDetail}",
                    )
                    AndroidQQLoginRuntime.clearAuthListener(this)
                    continuation.resume(Result.failure(IllegalStateException(error?.errorMessage ?: "QQ auth error")))
                }

                override fun onCancel() {
                    DbgFriendAuthLog.fileLog("QQSdk", "response api=QQ.login result=canceled")
                    UserLog.fileLog(TAG, "QQ chain auth canceled")
                    AndroidQQLoginRuntime.clearAuthListener(this)
                    continuation.resume(Result.failure(AddFriendChainAuthCanceledException("QQ auth canceled")))
                }

                override fun onWarning(code: Int) {
                    DbgFriendAuthLog.warn("QQSdk") { "response api=QQ.login result=warning code=$code" }
                    UserLog.warn(TAG) { "QQ chain auth warning code=$code" }
                }
            }
            AndroidQQLoginRuntime.registerAuthListener(listener)
            continuation.invokeOnCancellation {
                DbgFriendAuthLog.fileLog("QQSdk", "response api=QQ.login result=coroutineCanceled")
                UserLog.fileLog(TAG, "QQ chain auth coroutine canceled")
                AndroidQQLoginRuntime.clearAuthListener(listener)
            }
            tencent.logout(activity)
            activity.intent.putExtra(AuthAgent.KEY_FORCE_QR_LOGIN, false)
            tencent.login(activity, QQ_CHAIN_AUTH_SCOPE, listener, false)
        }
    }

    private fun shouldSubmitQQChainBind(openId: String): Boolean {
        val login = appLogin()
        val isMainQQLogin = login.getMainLoginUserInfo().isQQStrictLogin()
        if (isMainQQLogin) {
            val currentOpenId = login.userOpenId()
            if (currentOpenId.isNotBlank() && openId.isNotBlank() && currentOpenId != openId) {
                DbgFriendAuthLog.fileLog(
                    "QQSdk",
                    "main-chain auth account mismatch currentOpenIdLen=${currentOpenId.length} authOpenIdLen=${openId.length}",
                )
                UserLog.fileLog(TAG, "QQ chain auth account mismatch")
                throw AddFriendAuthAccountMismatchException(ADD_FRIEND_AUTH_TYPE_QQ)
            }
            DbgFriendAuthLog.fileLog("QQSdk", "skip stSetChainAuthBindReq for QQ main-chain auth")
            UserLog.fileLog(TAG, "QQ chain auth skipChainBind because current login is QQ")
        }
        return !isMainQQLogin
    }

    private suspend fun requestWechatChainAuth(): Result<AddFriendChainAuthRequest> {
        val api = AndroidWXLoginRuntime.getWxApi()
            ?: return Result.failure(IllegalStateException("WX SDK is not initialized").also {
                UserLog.error(TAG, "WX chain auth start failed: WX SDK is not initialized", it)
            })
        if (!api.isWXAppInstalled) {
            val error = IllegalStateException("WX is not installed")
            UserLog.error(TAG, "WX chain auth start failed: WX is not installed", error)
            return Result.failure(error)
        }
        UserLog.fileLog(TAG, "WX chain auth start installed=true supportApi=${api.wxAppSupportAPI}")
        UserLog.debug(TAG) { "WX chain auth sendReq start scope=$WX_FRIEND_SCOPE state=$WX_CHAIN_AUTH_STATE" }
        return suspendCancellableCoroutine { continuation ->
            val handler = object : IWXAPIEventHandler {
                override fun onReq(req: BaseReq?) = Unit

                override fun onResp(resp: BaseResp?) {
                    if (resp !is SendAuth.Resp || resp.state != WX_CHAIN_AUTH_STATE) {
                        UserLog.debug(TAG) {
                            "WX chain auth ignore response type=${resp?.let { it::class.simpleName }} state=${(resp as? SendAuth.Resp)?.state}"
                        }
                        return
                    }
                    UserLog.fileLog(TAG, "WX chain auth onResp errCode=${resp.errCode} codeLen=${resp.code?.length ?: 0}")
                    AndroidWXLoginRuntime.registerEventHandler(null)
                    when (resp.errCode) {
                        BaseResp.ErrCode.ERR_OK -> {
                            val code = resp.code.orEmpty()
                            if (code.isBlank()) {
                                continuation.resume(Result.failure(IllegalStateException("WX auth code is empty")))
                                return
                            }
                            CoroutineScope(Dispatchers.Main).launch {
                                UserLog.fileLog(
                                    TAG,
                                    "WX chain auth success codeLen=${code.length}",
                                )
                                continuation.resume(
                                    Result.success(
                                        AddFriendChainAuthRequest(
                                            authType = ADD_FRIEND_AUTH_TYPE_WECHAT,
                                            openToken = code,
                                            submitToChainBind = shouldSubmitWechatChainBind(),
                                        )
                                    )
                                )
                            }
                        }
                        BaseResp.ErrCode.ERR_USER_CANCEL ->
                            continuation.resume(Result.failure(AddFriendChainAuthCanceledException("WX auth canceled")))
                        else ->
                            continuation.resume(Result.failure(IllegalStateException("WX auth failed: code=${resp.errCode}, msg=${resp.errStr}")))
                    }
                }
            }
            AndroidWXLoginRuntime.registerEventHandler(handler)
            continuation.invokeOnCancellation {
                UserLog.fileLog(TAG, "WX chain auth coroutine canceled")
                AndroidWXLoginRuntime.registerEventHandler(null)
            }
            val request = SendAuth.Req().apply {
                scope = WX_FRIEND_SCOPE
                state = WX_CHAIN_AUTH_STATE
            }
            if (!api.sendReq(request)) {
                UserLog.error(TAG, "WX chain auth sendReq failed")
                AndroidWXLoginRuntime.registerEventHandler(null)
                continuation.resume(Result.failure(IllegalStateException("WX send auth request failed")))
            } else {
                UserLog.fileLog(TAG, "WX chain auth sendReq success")
            }
        }
    }

    private fun shouldSubmitWechatChainBind(): Boolean {
        val isMainWechatLogin = isMainWechatLogin()
        if (isMainWechatLogin) {
            UserLog.fileLog(TAG, "WX chain auth skipChainBind because current login is WX")
        }
        return !isMainWechatLogin
    }

    private fun isMainWechatLogin(): Boolean {
        return appLogin().getMainLoginUserInfo().isWxStrictLogin()
    }

    private fun currentActivity(): Activity? {
        return runCatching { LocalKmmContext.getRealContext() as? Activity }.getOrNull()
    }

    private const val TAG = "AddFriendAuthBridge"
    private const val QQ_CHAIN_AUTH_SCOPE = QQ_FRIEND_CHAIN_AUTH_SCOPE
    private const val WX_FRIEND_SCOPE = "snsapi_friend"
    private const val WX_CHAIN_AUTH_STATE = "chain_auth"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_OPEN_ID = "open_id"
    private const val KEY_OPENID = "openid"
}
