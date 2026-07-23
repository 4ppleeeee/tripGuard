package com.tencent.news.core.view


actual fun <T : Any> expectCreateBreakCircleRef(target: T): IBreakCircleRef<T?> =
    OhosBreakCircleRef(target)

private class OhosBreakCircleRef<T>(private val _target: T) : IBreakCircleRef<T> {
    override fun getTarget(): T? = _target
}