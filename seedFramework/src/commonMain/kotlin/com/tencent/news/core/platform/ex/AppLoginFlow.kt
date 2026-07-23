package com.tencent.news.core.platform.ex

import com.tencent.news.core.platform.api.IAppLoginFlow
import com.tencent.news.core.platform.api.IAppLoginStateChangedListener
import com.tencent.news.core.platform.api.appLogin
import com.tencent.news.core.platform.api.isMainLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

fun appLoginFlow(): IAppLoginFlow = AppLoginFlow

// 全局可以通过flow方式监听登录
private object AppLoginFlow : IAppLoginFlow, IAppLoginStateChangedListener {

    override val isLogin = MutableStateFlow(isMainLogin())

    init {
        appLogin().createSubscriber().subscribe(once = false, this)
    }

    override fun onStateChanged(accountType: String, isLogin: Boolean) {
        this.isLogin.update { isLogin }
    }

}