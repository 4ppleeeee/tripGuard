package com.tencent.news.core.platform


actual open class BasePlatformModel : IKmmDeepClone {

    actual override fun kmmDeepClone(): Any? {
        return appDeepClone(this) // kmm内不支持，需要宿主注入
    }
}