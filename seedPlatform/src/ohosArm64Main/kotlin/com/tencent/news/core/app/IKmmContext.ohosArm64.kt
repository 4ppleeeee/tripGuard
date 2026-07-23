package com.tencent.news.core.app

import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias OhosContext = JSValue

@KNCallback
actual interface IKmmContext {

    fun getWindowId(): Int = 0
}

val IKmmContext.ohosContext: OhosContext get() = (this as IKmmContextProxy)._value

