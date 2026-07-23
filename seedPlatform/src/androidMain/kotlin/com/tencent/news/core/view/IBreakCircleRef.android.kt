package com.tencent.news.core.view

import java.lang.ref.WeakReference


actual fun <T : Any> expectCreateBreakCircleRef(target: T): IBreakCircleRef<T?> =
    AndroidBreakCircleRef(target)

private class AndroidBreakCircleRef<T>(target: T) : IBreakCircleRef<T?> {
    private val weakRef = WeakReference(target)
    override fun getTarget(): T? = weakRef.get()
}