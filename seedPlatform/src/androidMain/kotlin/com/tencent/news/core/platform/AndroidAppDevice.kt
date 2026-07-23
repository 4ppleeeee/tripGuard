package com.tencent.news.core.platform

import android.os.Build
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.constants.ScreenType
import com.tencent.news.core.platform.api.AndroidRomType
import com.tencent.news.core.platform.api.IAndroidDevice
import com.tencent.news.core.platform.api.IAppDevice

@KmmInternalApi
internal fun setupAndroidAppDevice() {
    QnPlatformLogic.appDevice = AndroidAppDevice
}

private object AndroidAppDevice : IAppDevice {
    override fun getAndroidRom(): IAndroidDevice = AndroidRomDevice
}

private object AndroidRomDevice : IAndroidDevice {
    override fun getType(): AndroidRomType {
        return when {
            AndroidRomUtil.isVivo() -> AndroidRomType.VIVO
            AndroidRomUtil.isOppo() -> AndroidRomType.OPPO
            AndroidRomUtil.isHuawei() -> AndroidRomType.HUAWEI
            AndroidRomUtil.isHonor() -> AndroidRomType.HONOR
            AndroidRomUtil.isXiaomi() -> AndroidRomType.XIAOMI
            AndroidRomUtil.isSamsung() -> AndroidRomType.SAMSUNG
            AndroidRomUtil.isMeizu() -> AndroidRomType.MEIZU
            else -> AndroidRomType.ANDROID
        }
    }

    override fun isHarmony(): Boolean = AndroidManufacturerUtil.isHarmony()

    override fun getScreenType(): ScreenType = ScreenType.PHONE

    override fun getScreenCount(): Int = 1

    override fun getInstallChannel(): String = ""
}

private object AndroidRomUtil {
    fun isHuawei(): Boolean = isThisDevice("HUAWEI")

    fun isVivo(): Boolean = isThisDevice("vivo")

    fun isOppo(): Boolean = isThisDevice("OPPO") || isThisDevice("realme")

    fun isXiaomi(): Boolean = AndroidManufacturerUtil.isXiaomi()

    fun isMeizu(): Boolean = isThisDevice("Meizu")

    fun isSamsung(): Boolean = isThisDevice("samsung")

    fun isHonor(): Boolean = AndroidManufacturerUtil.isHonor()

    private fun isThisDevice(name: String): Boolean {
        return AndroidManufacturerUtil.getManufacturer().equals(name, ignoreCase = true)
    }
}

private object AndroidManufacturerUtil {
    private const val HARMONY = "harmony"

    private var manufacture: String? = null

    fun getManufacturer(): String {
        if (manufacture.isNullOrEmpty() || manufacture == Build.UNKNOWN) {
            manufacture = Build.MANUFACTURER
        }
        return manufacture.orEmpty()
    }

    fun isXiaomi(): Boolean {
        val manufacture = Build.MANUFACTURER
        val brand = Build.BRAND
        return "xiaomi".equals(manufacture, ignoreCase = true) ||
                "xiaomi".equals(brand, ignoreCase = true) ||
                "redmi".equals(manufacture, ignoreCase = true) ||
                "redmi".equals(brand, ignoreCase = true) ||
                "meitu".equals(manufacture, ignoreCase = true) ||
                "meitu".equals(brand, ignoreCase = true)
    }

    fun isHarmony(): Boolean {
        return try {
            val clz = Class.forName("com.huawei.system.BuildEx")
            val method = clz.getMethod("getOsBrand")
            HARMONY == method.invoke(clz)
        } catch (ignore: Exception) {
            false
        }
    }

    fun isHonor(): Boolean {
        return "HONOR".equals(Build.BRAND, ignoreCase = true) ||
                "HONOR".equals(Build.MANUFACTURER, ignoreCase = true)
    }
}
