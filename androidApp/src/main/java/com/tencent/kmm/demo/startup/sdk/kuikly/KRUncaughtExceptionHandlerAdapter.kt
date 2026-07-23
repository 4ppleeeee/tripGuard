package com.tencent.kmm.demo.startup.sdk.kuikly

import android.util.Log
import com.tencent.kuikly.core.render.android.adapter.IKRUncaughtExceptionHandlerAdapter

object KRUncaughtExceptionHandlerAdapter : IKRUncaughtExceptionHandlerAdapter {

    private const val TAG = "KRExceptionHandler"

    override fun uncaughtException(throwable: Throwable) {
        Log.e(TAG, "KR error: ${throwable.stackTraceToString()}")
    }

}
