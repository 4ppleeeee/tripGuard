package com.tencent.news.core.platform.ex

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.extension.getCurTimestampMillis
import com.tencent.news.core.list.trace.AppLocationLog
import com.tencent.news.core.platform.api.ILocationCallBack
import com.tencent.news.core.platform.api.IPermissionCallback
import com.tencent.news.core.platform.api.LocationPermissionScenes
import com.tencent.news.core.platform.api.appLocation
import com.tencent.news.core.platform.api.appPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private var lastAdCodeTs: Long = 0
private var adCode: String? = null

/**
 * 根据[maxAge]获取adCode，如果距离上次获取adCode超过[maxAge]毫秒就重新获取最新的adCode，否则返回上次的值
 */
fun getAdCodeWithAge(maxAge: Long): String? {
    if (lastAdCodeTs == 0L || getCurTimestampMillis() - lastAdCodeTs > maxAge) {
        lastAdCodeTs = getCurTimestampMillis()
        // 通过appLocation获取adCode时会触发定位，会造成preload无adcode，但真实请求有adcode，参数不一致无法接力
        adCode = appLocation().silentGetAdCode()
    }
    return adCode
}


/**
 * 申请定位权限，有权限的情况直接回调[callback]。
 * @param context 当前页面
 * @param scenes 当前场景，用来做频控和隐私管控
 * @param failfast 没有权限的时候，不等用户确认，直接回调[callback]
 * @param isForceRequestPermission 权限被拒绝的时候，弹出自定义弹窗提示用户去打开权限
 * @param callback 权限申请通过或拒绝之后的回调
 */
suspend fun requestLocation(
    context: IKmmContext?,
    @LocationPermissionScenes scenes: Int,
    failfast: Boolean = true,
    requestLocationOnGrand: Boolean = true,
    isForceRequestPermission: Boolean,
    callback: ILocationCallBack?
) = withContext(Dispatchers.Main) {
    context ?: run {
        callback?.onLocationRequested("")
        AppLocationLog.error("【requestLocation 传参错误】scenes=${scenes}，context=null")
        return@withContext
    }

    if (appPermission.hasLocationPermission(context, scenes)) {
        AppLocationLog.fileLog("【已有定位权限】场景=${scenes}，发起定位请求")
        appLocation().requestLocation(
            context, scenes, isForceRequestPermission,
            requestLocationOnGrand, WrapperCallback(scenes, callback)
        )
    } else {
        if (failfast) {
            callback?.onLocationRequested("")
        }
        AppLocationLog.fileLog("【未获取定位权限】场景=${scenes}，向用户发起定位授权")
        appPermission.requestLocationPermission(
            context = context,
            scenes = scenes,
            isForceRequestPermission = isForceRequestPermission,
            callback = object : IPermissionCallback {
                override fun onPermissionGranted() {
                    AppLocationLog.fileLog("【定位权限已授权】场景=${scenes}，准备发起定位")
                    CoroutineScope(Dispatchers.Default).launch { // 这里scope要注意不能失效
                        appLocation().requestLocation(
                            context, scenes, isForceRequestPermission,
                            requestLocationOnGrand, WrapperCallback(scenes, callback)
                        )
                    }
                }

                override fun onPermissionRejected(reason: String) {
                    if (!failfast) {
                        callback?.onLocationRequested("")
                    }
                    AppLocationLog.error("【定位权限被拒绝】场景=${scenes}，原因=${reason}")
                }
            })
    }
}

/**
 * 检查是否有定位权限
 * @param context 上下文
 * @param scenes 使用场景
 * @return 是否已授权定位权限
 */
fun hasLocationPermission(
    context: IKmmContext?,
    @LocationPermissionScenes scenes: Int,
): Boolean {
    context ?: run {
        AppLocationLog.error("【hasLocationPermission 传参错误】scenes=${scenes}，context=null")
        return false
    }
    return appPermission.hasLocationPermission(context, scenes)
}

private class WrapperCallback(
    @LocationPermissionScenes
    val scenes: Int,
    val originCallback: ILocationCallBack?
) : ILocationCallBack {
    override fun onLocationRequested(adCode: String) {
        AppLocationLog.fileLog("【定位成功】场景=${scenes}，adCode=${adCode}")
        originCallback?.onLocationRequested(adCode)
    }
}