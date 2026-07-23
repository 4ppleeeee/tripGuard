package com.tencent.news.core.login.extension

import com.tencent.news.core.platform.api.appLogin

object IAppLoginEx {

    fun isUserLogin(): Boolean = appLogin().getMainLoginUserInfo().isStrictLogin()

    fun getSuid(): String = appLogin().getSuid()

    fun getLoginSuid(): String = if (isUserLogin()) getSuid() else ""

}