package com.tencent.news.core.platform.api

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic

val appPermission get() = QnPlatformLogic.appPermission ?: DefaultAppPermission()

interface IAppPermission {

    /**
     * 是否有定位权限
     */
    fun hasLocationPermission(context: IKmmContext, @LocationPermissionScenes scenes: Int): Boolean

    /**
     * 申请定位权限，有权限的情况直接回调[callback]。
     * @param context 当前页面
     * @param scenes 当前场景，用来做频控和隐私管控
     * @param isForceRequestPermission 权限被拒绝的时候，弹出App自定义弹窗提示用户打开权限
     * @param callback 权限申请通过或拒绝之后的回调
     */
    fun requestLocationPermission(
        context: IKmmContext,
        @LocationPermissionScenes scenes: Int,
        isForceRequestPermission: Boolean = false,
        callback: IPermissionCallback,
    )


}

private class DefaultAppPermission() : IAppPermission {
    override fun hasLocationPermission(context: IKmmContext, @RecordVoiceScenes scenes: Int): Boolean {
        return true
    }

    override fun requestLocationPermission(
        context: IKmmContext,
        scenes: Int,
        isForceRequestPermission: Boolean,
        callback: IPermissionCallback,
    ) {
        callback.onPermissionGranted()
    }
}

interface IPermissionCallback {
    fun onPermissionGranted()

    fun onPermissionRejected(reason: String)
}

annotation class RecordVoiceScenes {
    companion object {
        const val TTS_TIMBRE = 1        // 早报自定义音色场景
    }
}
