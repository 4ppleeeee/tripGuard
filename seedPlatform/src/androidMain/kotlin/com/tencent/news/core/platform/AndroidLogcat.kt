package com.tencent.news.core.platform

import android.util.Log

object AndroidLogcat : ICommonLog {
    override fun logV(tag: String, msg: String) {
        Log.v(tag, msg)
    }

    override fun logD(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    override fun logI(tag: String, msg: String) {
        Log.i(tag, msg)
    }

    override fun logW(tag: String, msg: String) {
        Log.w(tag, msg)
    }

    override fun logE(tag: String, msg: String, throwable: Throwable?) {
        Log.e(tag, msg, throwable)
    }
}