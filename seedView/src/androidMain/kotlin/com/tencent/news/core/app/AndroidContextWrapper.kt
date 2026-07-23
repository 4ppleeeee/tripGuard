package com.tencent.news.core.app

import android.content.Context

class AndroidContextWrapper(val context: Context) : IKmmContext

fun IKmmContext?.getRealContext(): Context? {
    return if (this is AndroidContextWrapper) {
        this.context
    } else {
        this as? Context
    }
}

fun Context.getKmmContext(): IKmmContext {
    if (this is IKmmContext) {
        return this
    }
    return AndroidContextWrapper(this)
}