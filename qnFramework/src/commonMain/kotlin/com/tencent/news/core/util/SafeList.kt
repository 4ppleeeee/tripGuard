package com.tencent.news.core.util

import com.tencent.news.core.extension.safeList
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

// 用法 var testStrList: List<String> by SafeList(data::test_str_list)
class SafeList<T>(private val originProperty: KMutableProperty0<List<T?>?>) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): List<T> =
        safeList(originProperty.get())

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T?>?) =
        originProperty.set(safeList(value))

}