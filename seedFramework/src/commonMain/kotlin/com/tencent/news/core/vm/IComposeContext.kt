package com.tencent.news.core.vm

interface IComposeContext {

    fun findNativeView(nativeViewRef: Int): Any?

}