package com.tencent.kmm.demo.setup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.getRealContext
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IAppLocation
import com.tencent.news.core.platform.api.IAppPermission
import com.tencent.news.core.platform.api.ILocationCallBack
import com.tencent.news.core.platform.api.IPermissionCallback
import com.tencent.news.core.platform.qnFileLog
import com.tencent.kmm.demo.KRApplication

private const val TAG = "AndroidLocationPlatform"
private const val REQUEST_CODE_APP_LOCATION_PERMISSION = 0x7E16

private var appLocationPermissionCallback: IPermissionCallback? = null

@KmmInternalApi
internal fun setupAndroidLocationPlatformBridge() {
    QnPlatformLogic.appPermission = AndroidLocationPlatformBridge
    QnPlatformLogic.appLocation = AndroidLocationPlatformBridge
}

@Suppress("UNUSED_PARAMETER")
fun handleAndroidAppLocationPermissionResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
): Boolean {
    if (requestCode != REQUEST_CODE_APP_LOCATION_PERMISSION) return false
    val callback = appLocationPermissionCallback ?: return true
    appLocationPermissionCallback = null
    if (AndroidLocationPlatformBridge.hasLocationPermission(
            KRApplication.application,
            DEFAULT_LOCATION_SCENE
        )
    ) {
        callback.onPermissionGranted()
    } else {
        callback.onPermissionRejected("location permission denied")
    }
    return true
}

private object AndroidLocationPlatformBridge : IAppPermission, IAppLocation {

    private val appContext: Context
        get() = KRApplication.application

    override fun hasLocationPermission(context: IKmmContext, scenes: Int): Boolean {
        val realContext = context.getRealContext() as? Context ?: appContext
        return hasAnyLocationPermission(realContext)
    }

    override fun requestLocationPermission(
        context: IKmmContext,
        scenes: Int,
        isForceRequestPermission: Boolean,
        callback: IPermissionCallback,
    ) {
        if (hasLocationPermission(context, scenes)) {
            callback.onPermissionGranted()
            return
        }
        val activity = context.getRealContext() as? Activity ?: currentActivity()
        if (activity == null) {
            qnFileLog()?.logW(TAG, "requestLocationPermission ignored: no foreground Activity")
            callback.onPermissionRejected("no foreground Activity")
            return
        }
        appLocationPermissionCallback?.onPermissionRejected("location permission request replaced")
        appLocationPermissionCallback = callback
        runCatching {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                REQUEST_CODE_APP_LOCATION_PERMISSION,
            )
        }.onFailure { error ->
            qnFileLog()?.logE(TAG, "requestPermissions failed", error)
            if (appLocationPermissionCallback === callback) {
                appLocationPermissionCallback = null
            }
            callback.onPermissionRejected("request permission failed")
        }
    }

    override suspend fun requestLocation(
        context: IKmmContext?,
        scenes: Int,
        isForceRequestPermission: Boolean,
        requestLocationOnGrand: Boolean,
        callback: ILocationCallBack?,
    ) {
        callback?.onLocationRequested("")
    }

    override fun isSystemLocationEnabled(): Boolean {
        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    override fun openLocationSettings() {
        val context = currentActivity() ?: appContext
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { qnFileLog()?.logE(TAG, "openLocationSettings failed", it) }
    }

    override fun enterModule(scenes: Int) {
        qnFileLog()?.logI(TAG, "enterModule scenes=$scenes")
    }

    override fun leaveModule(scenes: Int) {
        qnFileLog()?.logI(TAG, "leaveModule scenes=$scenes")
    }
}

private const val DEFAULT_LOCATION_SCENE = 1

private fun currentActivity(): Activity? =
    runCatching { LocalKmmContext.getRealContext() as? Activity }.getOrNull()

private fun hasAnyLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
