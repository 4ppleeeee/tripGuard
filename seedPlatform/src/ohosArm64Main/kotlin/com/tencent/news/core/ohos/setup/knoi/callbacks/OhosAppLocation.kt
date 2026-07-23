package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppLocation
import com.tencent.news.core.platform.api.ILocationCallBack
import com.tencent.news.core.platform.qnLogcat
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppLocation = JSValue

/**
 * 注入鸿蒙端 [IAppLocation] 实现。
 *
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧基于 @kit.LocationKit 的
 * geoLocationManager.getCurrentLocation 真实实现桥接到 KMP 层。
 *
 * 【关于 adCode】
 *  鸿蒙系统 API 不直接返回国家统计局 6 位 adCode（如 "440305"），
 *  geoLocationManager.getAddressesFromLocation 仅返回行政区划描述字符串
 *  （如 "广东省深圳市南山区"）。因此当前 Provider 的 silentGetAdCode 统一返回空串，
 *  与 Android/iOS 未注入时的兜底行为一致。如后续业务需要 adCode，
 *  可接入腾讯地图/高德 SDK 做经纬度→adCode 的二次转换。
 *
 * ArkTS 侧通过 Kommon.setup 调用 getHarmonyStartupProvider().setupAppLocation(new
 * OhosAppLocationCallback()) 注入实现。
 */
fun setupOhosAppLocation(location: IOhosAppLocation) {
    QnPlatformLogic.appLocation = OhosAppLocationProvider(location.asOhosAppLocation())
}

/**
 * Kotlin 侧的 [IAppLocation] 实现。
 *
 * - requestLocation：把调用桥到 ArkTS 侧，拿到经纬度后转发给业务回调。
 *   注意：当前不返回 adCode，因为鸿蒙系统 API 不直接提供该字段，
 *   回调入参 adCode 透传空串（与 iOS 未注入兜底一致），业务侧会自行降级。
 * - silentGetAdCode：鸿蒙端不做缓存，直接返回空串。
 */
private class OhosAppLocationProvider(
    private val native: OhosAppLocation,
) : IAppLocation {

    private companion object {
        const val TAG = "OhosAppLocation"
    }

    override suspend fun requestLocation(
        context: IKmmContext?,
        scenes: Int,
        isForceRequestPermission: Boolean,
        requestLocationOnGrand: Boolean,
        callback: ILocationCallBack?,
    ) {
        if (callback == null) return
        runCatching {
            native.requestLocation(scenes.toString(), scenes.toDouble()) { success, _, _, adCode ->
                if (success) {
                    callback.onLocationRequested(adCode)
                } else {
                    callback.onLocationRequested("")
                }
            }
        }.onFailure {
            qnLogcat()?.logE(TAG, "requestLocation failed", it)
            callback.onLocationRequested("")
        }
    }

    override fun silentGetAdCode(scenes: Int): String = ""
}

/**
 * ArkTS 侧定位能力实现接口。
 *
 * knoi 编译时会自动生成 ArkTS 侧的接口定义，ArkTS 侧 OhosAppLocationCallback
 * 实现该接口并通过 getHarmonyStartupProvider().setupAppLocation 注入。
 *
 * 设计要点：
 *  - ArkTS 侧基于 geoLocationManager.getCurrentLocation 发起单次定位；
 *  - 回调参数尽量扁平：success + 经纬度 + adCode（当前恒为空串，预留后续接入地图 SDK 的扩展位）；
 *  - scene 使用业务命名空间字符串，避免不同业务之间的数字枚举冲突；
 *  - legacyCode 保留旧 LocationPermissionScenes 数字值，兼容原生侧埋点/频控。
 */
@KNCallback
interface OhosAppLocation {

    /**
     * 发起单次定位请求。
     *
     * @param scene 场景标识，ArkTS 侧可用于埋点/频控
     * @param legacyCode 旧 int 场景值。0 表示无旧值。
     * @param onResult 定位结果回调：
     *     - success：是否定位成功
     *     - latitude：纬度（定位失败时可为 0.0）
     *     - longitude：经度（定位失败时可为 0.0）
     *     - adCode：行政区划编码（鸿蒙端当前恒为空串，预留扩展）
     */
    fun requestLocation(
        scene: String,
        legacyCode: Double,
        onResult: (success: Boolean, latitude: Double, longitude: Double, adCode: String) -> Unit,
    )
}
