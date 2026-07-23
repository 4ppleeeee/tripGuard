package com.tencent.news.core.extension

import com.tencent.news.core.platform.network.NetworkRelayState


data class ResultEx(
    var succeed: Boolean,
    var msg: String = "",
    // 接力状态，为空则肯定未参与接力
    var relayState: NetworkRelayState? = null,
    var error: Throwable? = null,
    var errorCode: Int = ResultCodeEx.NONE,
) {
    fun getLogStr(): String {
        return toString()
    }
}

fun successResult(msg: String = ""): ResultEx {
    return ResultEx(
        succeed = true,
        msg = msg,
        error = null,
        errorCode = ResultCodeEx.NONE
    )
}

fun errorResult(msg: String = ""): ResultEx {
    return ResultEx(
        succeed = false,
        msg = msg,
        error = null,
        errorCode = ResultCodeEx.ERROR
    )
}

fun errorResult(msg: String = "", errorCode: Int = ResultCodeEx.ERROR): ResultEx {
    return ResultEx(
        succeed = false,
        msg = msg,
        error = null,
        errorCode = errorCode
    )
}

fun cancelResult(): ResultEx {
    return ResultEx(
        succeed = false,
        msg = "canceled",
        error = null,
        errorCode = ResultCodeEx.CANCEL
    )
}

object ResultCodeEx {

    const val NONE = 0

    // 内置错误码取个很特殊的值，防止重复
    const val ERROR = -9527001
    const val CANCEL = -9527002

    const val JSON_PARSE_FAIL = -9527003
    const val LIST_EMPTY = -9527004

}