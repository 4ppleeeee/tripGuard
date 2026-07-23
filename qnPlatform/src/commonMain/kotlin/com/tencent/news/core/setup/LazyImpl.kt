package com.tencent.news.core.setup

import com.tencent.news.core.annotation.KmmInternalApi


// todo 【注意】依赖注入时：很多manager实现类初始化时会有执行逻辑，容易导致初始化时机不对； 应统一使用延迟方式注入
typealias LazyImpl<T> = () -> T

interface ILazyImplHolder<T> {
    @KmmInternalApi
    var instance: LazyImpl<T>
}

@KmmInternalApi
fun <T> ILazyImplHolder<T>.registerImpl(impl: LazyImpl<T>) {
    instance = impl
}

@KmmInternalApi
fun <T> ILazyImplHolder<T>.get(): T = instance()