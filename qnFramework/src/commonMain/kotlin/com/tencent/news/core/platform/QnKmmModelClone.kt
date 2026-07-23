package com.tencent.news.core.platform


object QnKmmModelClone : IPlatformInject {
    var cloneHelper: IQnKmmModelClone? = null
}

interface IQnKmmModelClone {
    fun kmmDeepClone(origin: Any): Any?
}

internal fun appDeepClone(origin: Any): Any? {
    return QnKmmModelClone.cloneHelper?.kmmDeepClone(origin)
}