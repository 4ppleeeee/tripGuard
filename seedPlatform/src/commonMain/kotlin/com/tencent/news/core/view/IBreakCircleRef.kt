package com.tencent.news.core.view

import com.tencent.news.core.annotation.KmmInternalApi

// 用于解决ios对象循环强引用，导致内存泄漏（安卓的回收机制不会有问题）
interface IBreakCircleRef<T> {
    fun getTarget(): T?
}

@OptIn(KmmInternalApi::class)
fun <T : Any> createBreakCircleRef(target: T): IBreakCircleRef<T?> =
    expectCreateBreakCircleRef(target)

@KmmInternalApi
expect fun <T : Any> expectCreateBreakCircleRef(target: T): IBreakCircleRef<T?>