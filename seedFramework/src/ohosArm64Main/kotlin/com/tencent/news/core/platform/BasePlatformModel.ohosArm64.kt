package com.tencent.news.core.platform

actual open class BasePlatformModel actual constructor() : IKmmDeepClone {
    actual override fun kmmDeepClone(): Any? {
        return appDeepClone(this)
    }
}