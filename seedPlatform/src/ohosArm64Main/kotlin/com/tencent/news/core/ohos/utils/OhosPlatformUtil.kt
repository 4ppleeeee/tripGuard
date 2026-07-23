package com.tencent.news.core.ohos.utils

import com.tencent.news.core.extension.takeIfNotEmpty
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.devices.OH_AbilityRuntime_ApplicationContextGetBundleName
import platform.ohos.OH_GetBrand
import platform.ohos.OH_GetDeviceType
import platform.ohos.OH_GetOSFullName
import platform.ohos.OH_GetSdkApiVersion

internal object OhosPlatformUtil {

    /**
     * 是否是 debug 包，由宿主侧通过 onAppStartup 注入 BuildProfile.DEBUG
     */
    var isDebug: Boolean = false

    /**
     * 是否是RDM包（非正式发布包）
     */
    fun isRdm(): Boolean {
        return getPackageName().endsWith(".rdm")
    }

    /**
     * 获取应用包名
     * @return 应用包名，如果获取失败则返回空字符串
     */
    fun getPackageName(): String {
        val packageName = getString { buffer, bufferSize, writeLength ->
            OH_AbilityRuntime_ApplicationContextGetBundleName(buffer, bufferSize, writeLength.ptr)
        }
        return packageName.takeIfNotEmpty() ?: "com.tencent.kmm.demo"
    }

    /**
     * 获取应用名称
     */
    fun getAppName(): String {
        return "KMM Demo"
    }

    /**
     * 获取API版本，比如：21
     */
    fun getApiVersion(): Int {
        return OH_GetSdkApiVersion()
    }

    /**
     * 获取操作系统版本，比如：OpenHarmony-5.0.5.165
     */
    fun getOsFullName(): String {
        return OH_GetOSFullName()?.toKString() ?: ""
    }

    /**
     * 获取品牌信息
     */
    fun getHardware(): String {
        return OH_GetBrand()?.toKString() ?: ""
    }

    /**
     * 获取设别类型
     */
    fun getRomType(): String {
        return OH_GetDeviceType()?.toKString() ?: ""
    }

}
