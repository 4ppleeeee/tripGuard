package com.tencent.news.core.platform


interface IKmmDeepClone {
    fun kmmDeepClone(): Any?
}

expect open class BasePlatformModel() : IKmmDeepClone {
    override fun kmmDeepClone(): Any?
}