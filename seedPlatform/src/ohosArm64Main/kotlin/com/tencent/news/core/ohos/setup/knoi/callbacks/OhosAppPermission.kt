package com.tencent.news.core.ohos.setup.knoi.callbacks

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppPermission
import com.tencent.news.core.platform.api.IPermissionCallback
import com.tencent.news.core.platform.qnLogcat
import com.tencent.tmm.knoi.annotation.KNCallback
import com.tencent.tmm.knoi.type.JSValue

typealias IOhosAppPermission = JSValue

/**
 * 注入鸿蒙端 [IAppPermission] 实现。
 *
 * 通过 knoi @KNCallback 机制，将 ArkTS 侧基于 @kit.AbilityKit 的
 * abilityAccessCtrl.createAtManager() 真实实现桥接到 KMP 层：
 *  - hasLocationPermission：查询 ohos.permission.LOCATION / APPROXIMATELY_LOCATION 授权态；
 *  - requestLocationPermission：拉起系统权限弹窗 requestPermissionsFromUser。
 *
 * 说明：
 *  - 鸿蒙 KMP 层只负责拉起系统权限窗；"被拒绝后跳转设置页" 的引导逻辑
 *    （isForceRequestPermission=true）由 ArkTS 侧根据 grantStatus 判定后决定，
 *    Kotlin 侧不处理自定义弹窗引导；
 *  - ArkTS 侧的 hasLocationPermission 为同步查询（基于 checkAccessTokenSync），
 *    requestLocationPermission 为异步申请并通过回调返回结果。
 */
fun setupOhosAppPermission(permission: IOhosAppPermission) {
    QnPlatformLogic.appPermission = OhosAppPermissionProvider(permission.asOhosAppPermission())
}

/**
 * Kotlin 侧的 [IAppPermission] 实现，负责：
     *  1. 接收定位权限场景，方便后续扩展埋点/频控；
 *  2. 把 ArkTS 侧回调（授权 / 拒绝 / 错误）转换为 IPermissionCallback 的
 *     onPermissionGranted / onPermissionRejected。
 */
private class OhosAppPermissionProvider(
    private val native: OhosAppPermission,
) : IAppPermission {

    private companion object {
        const val TAG = "OhosAppPermission"

        // ArkTS 侧回调的授权结果状态码，对齐鸿蒙 abilityAccessCtrl.GrantStatus：
        //   PERMISSION_DENIED = -1
        //   PERMISSION_GRANTED = 0
        const val GRANT_STATUS_GRANTED = 0
    }

    override fun hasLocationPermission(context: IKmmContext, scenes: Int): Boolean {
        return runCatching { native.hasLocationPermission() }
            .onFailure { qnLogcat()?.logE(TAG, "hasLocationPermission failed", it) }
            .getOrDefault(false)
    }

    override fun requestLocationPermission(
        context: IKmmContext,
        scenes: Int,
        isForceRequestPermission: Boolean,
        callback: IPermissionCallback,
    ) {
        val hasPermission = runCatching { native.hasLocationPermission() }.getOrDefault(false)
        if (hasPermission) {
            callback.onPermissionGranted()
            return
        }
        runCatching {
            native.requestLocationPermission(isForceRequestPermission) { grantStatus, reason ->
                if (grantStatus.toInt() == GRANT_STATUS_GRANTED) {
                    callback.onPermissionGranted()
                } else {
                    callback.onPermissionRejected(reason.ifEmpty { "permission denied" })
                }
            }
        }.onFailure {
            qnLogcat()?.logE(TAG, "requestLocationPermission failed", it)
            callback.onPermissionRejected(it.message ?: "permission request error")
        }
    }
}

/**
 * ArkTS 侧权限能力实现接口。
 *
 * knoi 编译时会自动生成 ArkTS 侧的接口定义，ArkTS 侧 OhosAppPermissionCallback
 * 实现该接口并通过 getHarmonyStartupProvider().setupAppPermission 注入。
 */
@KNCallback
interface OhosAppPermission {

    /**
     * 同步查询当前是否已授予定位权限（精确或模糊任一即算有权限）。
     *
     * ArkTS 侧基于 abilityAccessCtrl.createAtManager().checkAccessTokenSync(
     *     tokenId, 'ohos.permission.APPROXIMATELY_LOCATION') 实现。
     */
    fun hasLocationPermission(): Boolean

    /**
     * 异步申请定位权限。
     *
     * ArkTS 侧基于 atManager.requestPermissionsFromUser(uiContext,
     *     ['ohos.permission.APPROXIMATELY_LOCATION']) 实现。
     *
     * @param isForceRequestPermission 被拒后是否弹引导跳转系统设置页
     *     （ArkTS 侧通过判断 requestPermissionsFromUser 返回的
     *      authResults/dialogShownResults 结合此开关决定是否跳设置页）
     * @param onResult 结果回调：
     *     - grantStatus：0=授权，-1=拒绝
     *     - reason：拒绝原因（可为空字符串）
     */
    fun requestLocationPermission(
        isForceRequestPermission: Boolean,
        onResult: (grantStatus: Double, reason: String) -> Unit,
    )
}
