package com.tencent.news.core.platform.api

import com.tencent.news.core.app.constants.ScreenType
import com.tencent.news.core.platform.QnPlatformLogic

@Suppress("MaxLineLength")
interface IAppDevice {

    // 机型
    fun getAndroidRom(): IAndroidDevice? = null
    fun getIOSRom(): IIOSDevice? = null
    fun getHarmonyRom(): IHarmonyDevice? = null
}

abstract class AbsAppDevice : IAppDevice

fun appDevice(): IAppDevice = (QnPlatformLogic.appDevice ?: defaultAppDevice)

private val defaultAppDevice by lazy { DefaultAppDevice() }

class DefaultAppDevice : IAppDevice {
    override fun getAndroidRom(): IAndroidDevice? = null
    override fun getIOSRom(): IIOSDevice? = null
    override fun getHarmonyRom(): IHarmonyDevice? = null
}

interface IDevice

interface IIOSDevice : IDevice {
    fun getScreenType(): ScreenType
}

interface IHarmonyDevice : IDevice

interface IAndroidDevice : IDevice {
    fun getType(): AndroidRomType   // 安卓rom类型：华米ov等
    fun isHarmony(): Boolean        // 这个不是纯血鸿蒙，是老的安卓鸿蒙
    fun getScreenType(): ScreenType // 屏幕类型：普通手机、折叠屏、pad等
    fun getScreenCount(): Int       // 屏幕个数：例如折叠屏是2，三折叠是3
    fun getInstallChannel(): String // 安装渠道（渠道号）
}

enum class AndroidRomType(val nameStr: String) {
    ANDROID("android"),
    VIVO("vivo"),
    XIAOMI("xiaomi"),
    OPPO("oppo"),
    HUAWEI("huawei"),
    HONOR("honor"),
    SAMSUNG("samsung"),
    MEIZU("meizu");
}

fun IAppDevice.isAndroid() = appDevice().getAndroidRom() != null
fun IAppDevice.isIOS() = appDevice().getIOSRom() != null
fun IAppDevice.isHarmony() = appDevice().getHarmonyRom() != null

fun IAppDevice.isVivo() = appDevice().getAndroidRom()?.getType() == AndroidRomType.VIVO
fun IAppDevice.isXiaomi() = appDevice().getAndroidRom()?.getType() == AndroidRomType.XIAOMI
fun IAppDevice.isOppo() = appDevice().getAndroidRom()?.getType() == AndroidRomType.OPPO
fun IAppDevice.isHuaWei() = appDevice().getAndroidRom()?.getType() == AndroidRomType.HUAWEI
fun IAppDevice.isHonor() = appDevice().getAndroidRom()?.getType() == AndroidRomType.HONOR
fun IAppDevice.isSamsung() = appDevice().getAndroidRom()?.getType() == AndroidRomType.SAMSUNG
fun IAppDevice.isMeizu() = appDevice().getAndroidRom()?.getType() == AndroidRomType.MEIZU
