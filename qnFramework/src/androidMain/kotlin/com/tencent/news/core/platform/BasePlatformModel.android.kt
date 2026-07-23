package com.tencent.news.core.platform


actual open class BasePlatformModel : Cloneable, IKmmDeepClone {

    override fun clone(): Any {
        return kmmDeepClone() ?: superClone()
    }

    actual override fun kmmDeepClone(): Any? {
        return superClone()
    }

    // 防止子类重写后死循环
    private fun superClone(): Any {
        return super.clone()
    }

}