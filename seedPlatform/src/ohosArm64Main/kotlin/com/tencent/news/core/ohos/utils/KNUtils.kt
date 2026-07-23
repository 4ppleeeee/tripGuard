package com.tencent.news.core.ohos.utils

import io.ktor.utils.io.core.toByteArray
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.FloatVarOf
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.IntVarOf
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.linux.free

/**
 * 获取Int类型返回值的通用方法
 * @param defaultValue 默认值，当API调用失败时返回
 * @param impl API调用实现，接收IntVar指针参数，返回UInt类型的错误码
 * @return API返回的Int值，失败时返回defaultValue
 */
internal inline fun getInt(
    defaultValue: Int = 0,
    impl: NativePlacement.(CValuesRef<IntVarOf<Int>>) -> UInt
): Int = memScoped {
    val value = alloc<IntVar>()
    val result = impl(value.ptr)
    return@memScoped if (result == 0u) value.value else defaultValue
}

/**
 * 获取Float类型返回值的通用方法
 * @param defaultValue 默认值，当API调用失败时返回
 * @param impl API调用实现，接收FloatVar指针参数，返回UInt类型的错误码
 * @return API返回的Float值，失败时返回defaultValue
 */
internal inline fun getFloat(
    defaultValue: Float = 0f,
    impl: NativePlacement.(CValuesRef<FloatVarOf<Float>>) -> UInt
): Float = memScoped {
    val value = alloc<FloatVar>()
    val result = impl(value.ptr)
    return@memScoped if (result == 0u) value.value else defaultValue
}

/**
 * 获取String类型返回值的通用方法
 * @param bufferSize 缓冲区大小，默认256字节
 * @param impl API调用实现，接收buffer、bufferSize和writeLength参数，返回UInt类型的错误码
 * @return API返回的字符串，失败时返回空字符串
 */
internal fun getString(
    bufferSize: Int = 256,
    impl: (buffer: CValuesRef<ByteVarOf<Byte>>, bufferSize: Int, writeLength: IntVarOf<Int>) -> UInt
): String = memScoped {
    val buffer = allocArray<ByteVar>(bufferSize)
    val writeLength = alloc<IntVar>()

    val result = impl(buffer, bufferSize, writeLength)

    return if (result == 0u && writeLength.value > 0) {
        buffer.toKString()
    } else {
        ""
    }
}

internal fun String.toUByteArray(): UByteArray {
    return this.toByteArray().toUByteArray()
}

internal fun CPointer<ByteVarOf<Byte>>?.usingKString(): String? {
    this ?: return null
    val kString = this.toKString()
    free(this)
    return kString
}

internal inline fun <reified T : CPointed> CPointer<T>?.using(action: (T) -> Unit) {
    this ?: return
    action(this.pointed)
    free(this)
}