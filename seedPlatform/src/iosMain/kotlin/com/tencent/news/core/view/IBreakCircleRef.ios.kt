package com.tencent.news.core.view

import kotlin.native.ref.WeakReference


actual fun <T : Any> expectCreateBreakCircleRef(target: T): IBreakCircleRef<T?> =
    IOSBreakCircleRef(target)

private class IOSBreakCircleRef<T : Any>(target: T) : IBreakCircleRef<T?> {
    private val weakRef = WeakReference(target)

    override fun getTarget(): T? = weakRef.get()
}