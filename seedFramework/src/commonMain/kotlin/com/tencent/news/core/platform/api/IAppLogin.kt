package com.tencent.news.core.platform.api

import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.user.model.IUserInfo
import kotlinx.coroutines.flow.StateFlow


/**
 * 登录类型枚举
 */
object LoginType {
    const val INVALID = 0x0
    const val QQ = 0x001
    const val WX = 0x010
    const val OEW = 0x100
    const val ONLY_QQ = 0x1000
    const val ONLY_WX = 0x10000
    const val PHONE = 0x100000
    const val QQ_WX_PHONE = QQ or WX or PHONE
    const val QQ_WX = QQ or WX
    const val ALL = QQ or WX or PHONE or OEW
}

/**
 * 登录来源枚举
 */
object LoginFrom {
    const val COMMON = 0
    const val FAVORITE = 1
    const val LIKE = 2
    const val RADIO = 3
    const val SUBSCRIBE = 4
    const val FOLLOW_POST = 5
    const val FOLLOW_TAG = 6
    const val FOLLOW_QUESTION = 7
    const val FOLLOW_EVENT = 8
    const val FOLLOW_CP = 9
    const val AIGC_CHAT = 10
    const val AIGC_MORNING_POST = 11
    const val AIGC_PHONE_STREAM = 12
    const val GAME_RESERVE = 13
    const val PAYMENT = 14
    const val CHECK_IN = 15
    const val VIDEO_VIP_BANNER = 16
    const val AIGC_JIAO_ZHEN_IN_VIDEO = 17
    const val FOLLOW_RADIO = 18
    const val TAB_RADIO = 19
    const val UNDERLINE = 20
    const val UNDERLINE_COMMENT = 21
    const val GAME_CHECK_IN = 22
    const val FOLLOW_HOT_TRACE = 23
    const val FOLLOW_HOT_PUSH_TRACE = 24
    const val COIN_CENTER = 25
}

// flow版本的登录监听，在compose里用更方便
interface IAppLoginFlow {
    val isLogin: StateFlow<Boolean>
}

interface ILoginSubscriber {
    /**
     * 订阅登录态
     */
    fun subscribe(once: Boolean, listener: IAppLoginStateChangedListener)

    /**
     * 取消订阅登录态
     */
    fun unsubscribe(listener: IAppLoginStateChangedListener)
}


interface IAppLogin {
    // 获取用户主登录账号信息
    fun getMainLoginUserInfo(): ILoginUserInfo

    // 拉取登录弹窗
    fun triggerLogin(
        type: Int,
        from: Int,
        bossFrom: String = "",
        reportParams: Map<String, String> = emptyMap(),
        onComplete: (Boolean) -> Unit,
    )

    // 退出登录
    fun logout(dontToast: Boolean = false)

    // 监听登录态，每次调用都会创建新的订阅。订阅周期结束之后记得取消订阅。
    fun createSubscriber(): ILoginSubscriber

    // 获取用户id（qqOpenId、wxOpenId 等）
    fun userId(): String

    fun userInfo(): IUserInfo?

    fun getNickForComment(): String

    // 获取suid，登录则返回登录用户的suid，否则返回设备suid
    fun getSuid(): String

    // 获取当前登录类型
    fun getLoginType(): Int

    // 获取新闻token
    fun getNewsToken(): String

    // 更新新闻token
    fun updateNewsToken(onComplete: (Boolean) -> Unit)

    fun getCookieStr(): String = ""

}


interface IAppLoginStateChangedListener {
    fun onLogin(accountType: String) {
        onStateChanged(accountType, true)
    }

    fun onLogout(accountType: String) {
        onStateChanged(accountType, false)
    }

    fun onStateChanged(accountType: String, isLogin: Boolean) {}
}


@OptIn(KmmInternalApi::class)
fun userId(): String = QnFrameworkLogic.login?.userId() ?: ""


@OptIn(KmmInternalApi::class)
fun appLogin(): IAppLogin = QnFrameworkLogic.login ?: defaultAppLogin

// 新闻登录态是否有效（平时大多数用这个，理论上永不过期）
fun isMainLogin(): Boolean = appLogin().getMainLoginUserInfo().isStrictLogin()

// 特定渠道强登录态：普遍用于视频会员等特殊场景（不论主副账号）
fun isQQorWxLogin(): Boolean {
    return isQQLogin() || isWxLogin()
}

fun isQQLogin(): Boolean {
    val loginInfo = appLogin().getMainLoginUserInfo()
    return loginInfo.isQQStrictLogin()
}

fun isWxLogin(): Boolean {
    val loginInfo = appLogin().getMainLoginUserInfo()
    return loginInfo.isWxStrictLogin()
}

// 默认登录注入类
private val defaultAppLogin by lazy { DefaultAppLogin() }

class DefaultAppLogin : IAppLogin {
    override fun getMainLoginUserInfo(): ILoginUserInfo {
        return DefaultLoginUserInfo()
    }

    override fun triggerLogin(
        type: Int,
        from: Int,
        bossFrom: String,
        reportParams: Map<String, String>,
        onComplete: (Boolean) -> Unit,
    ) {
        // empty
    }

    override fun logout(dontToast: Boolean) {
        // empty
    }

    override fun createSubscriber(): ILoginSubscriber {
        return DefaultLoginSubscriber()
    }

    override fun userId(): String {
        return ""
    }

    override fun userInfo(): IUserInfo? {
        return null
    }

    override fun getNickForComment(): String {
        return ""
    }

    override fun getSuid(): String {
        return "8QIf3n9U6IcYsTne4wc="
    }

    override fun getLoginType(): Int {
        return LoginType.INVALID
    }

    override fun getNewsToken(): String {
        return ""
    }

    override fun updateNewsToken(onComplete: (Boolean) -> Unit) {
        onComplete(false)
    }
}

// 默认登录信息
class DefaultLoginUserInfo : ILoginUserInfo {
    override fun isStrictLogin(): Boolean {
        return true
    }

    override fun isQQStrictLogin(): Boolean = false
    override fun isWxStrictLogin(): Boolean = false
}

internal class DefaultLoginSubscriber : ILoginSubscriber {
    override fun subscribe(once: Boolean, listener: IAppLoginStateChangedListener) {
        // ignore
    }

    override fun unsubscribe(listener: IAppLoginStateChangedListener) {
        // ignore
    }
}
