package com.tencent.news.core.util

import com.tencent.news.core.extension.getNonNull
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty


// 方便处理dto代理字段的工具：原始数据为 String? 类型时，转换成安全的 String 类型
// 用法 var testStr: String by SafeString(data::test_str)
class SafeString(private val originProperty: KMutableProperty0<String?>) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
        originProperty.get().getNonNull()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) =
        originProperty.set(value.getNonNull())

}

// 这个专门兼容Java往Kotlin里设置空指针的情况
class SafeString2(private val originProperty: KMutableProperty0<String>) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String =
        originProperty.get().getNonNull()

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) =
        originProperty.set(value.getNonNull())

}

