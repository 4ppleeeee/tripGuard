package com.tencent.news.core.tads.model

import com.tencent.news.core.tads.constants.AdGdtClickActType
import com.tencent.news.core.tads.extension.IAdOrderVMEx.notifyDebugInfoChanged
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty


// 2个作用：
// 1. setValue后会触发 debugInfo 更新
// 2. 包装成state的属性，不参与深拷贝：一些cloneItem的操作，能保持这份数据始终引用到原始item
// （尤其是 gdtClickCount 这个参数，三链等跳转链路里会cloneItem，会导致 gdtClickCount 绑定不到列表item上）
class AdOrderPermanentState(private val adOrder: IKmmAdOrder) {

    var useAsyncClickReport = false // 已执行异步上报逻辑
    var clickCount = 0              // 点击次数：触发点击链路就+1

    // 真实上报次数：执行到上报时才+1
    private var _sspClickCount = 0
    var sspClickCount by debugInfoState(this::_sspClickCount)

    private var _gdtActType: AdGdtClickActType = AdGdtClickActType.DEFAULT_CLICK
    var gdtActType by debugInfoState(this::_gdtActType)

    private var _gdtClickCount = 0
    var gdtClickCount by debugInfoState(this::_gdtClickCount)

    private var _linkClickCount = 0
    var linkClickCount by debugInfoState(this::_linkClickCount)

    private fun <T> debugInfoState(
        originProperty: KMutableProperty0<T>,
        onSetValue: ((value: T) -> Unit)? = null
    ) = DebugInfoState(originProperty) { newValue ->
        onSetValue?.invoke(newValue)
        adOrder.notifyDebugInfoChanged()
    }

    private class DebugInfoState<T>(
        val originProperty: KMutableProperty0<T>,
        val onSetValue: (value: T) -> Unit
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T =
            originProperty.get()

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            originProperty.set(value)
            onSetValue(value)
        }
    }
}