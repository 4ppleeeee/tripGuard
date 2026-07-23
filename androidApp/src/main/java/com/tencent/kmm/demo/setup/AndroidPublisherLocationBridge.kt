package com.tencent.kmm.demo.setup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.getRealContext
import com.tencent.news.core.platform.qnFileLog
import com.tencent.kmm.demo.KRApplication
import com.tencent.kmm.demo.core.publisher.location.PublisherLocationGps
import com.tencent.kmm.demo.core.publisher.location.PublisherLocationPlatformBridge
import com.tencent.kmm.demo.core.publisher.location.PublisherLocationPlatformBridgeRegistry
import com.tencent.kmm.demo.core.publisher.location.PublisherLocationSignal
import java.net.NetworkInterface
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "AndroidPublisherLocation"
private const val REQUEST_CODE_LOCATION_PERMISSION = 0x7E14

private var locationPermissionContinuation: CancellableContinuation<Boolean>? = null

@KmmInternalApi
internal fun setupAndroidPublisherLocationBridge() {
    PublisherLocationPlatformBridgeRegistry.register(AndroidPublisherLocationBridge)
}

/** 分发发布器位置页运行时定位权限回调。 */
@Suppress("UNUSED_PARAMETER")
fun handlePublisherLocationPermissionResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
): Boolean {
    if (requestCode != REQUEST_CODE_LOCATION_PERMISSION) return false
    val continuation = locationPermissionContinuation ?: return true
    locationPermissionContinuation = null
    continuation.resume(AndroidPublisherLocationBridge.hasLocationPermission())
    return true
}

/** Android 发布器位置桥，提供前台定位权限、一次性定位和 WiFi MAC best-effort。 */
private object AndroidPublisherLocationBridge : PublisherLocationPlatformBridge {

    private val appContext: Context
        get() = KRApplication.application

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

    override fun hasLocationPermission(): Boolean =
        hasAnyLocationPermission(appContext)

    override suspend fun requestLocationPermission(): Boolean {
        if (hasLocationPermission()) return true
        val activity = currentActivity()
        if (activity == null) {
            qnFileLog()?.logW(TAG, "requestLocationPermission ignored: no foreground Activity")
            return false
        }
        return suspendCancellableCoroutine { continuation ->
            locationPermissionContinuation?.resume(hasLocationPermission())
            locationPermissionContinuation = continuation
            continuation.invokeOnCancellation {
                if (locationPermissionContinuation === continuation) {
                    locationPermissionContinuation = null
                }
            }
            runCatching {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                    REQUEST_CODE_LOCATION_PERMISSION,
                )
            }.onFailure { error ->
                qnFileLog()?.logE(TAG, "requestPermissions failed", error)
                if (locationPermissionContinuation === continuation) {
                    locationPermissionContinuation = null
                    continuation.resume(hasLocationPermission())
                }
            }
        }
    }

    override suspend fun getCurrentLocation(timeoutMs: Long): PublisherLocationSignal? {
        if (!isSystemLocationEnabled() || !hasLocationPermission()) {
            return PublisherLocationSignal(
                gps = null,
                wifiMacs = readWifiMacs(),
                systemLocationEnabled = isSystemLocationEnabled(),
                permissionGranted = hasLocationPermission(),
            )
        }
        val location = awaitCurrentLocation(timeoutMs)
        return PublisherLocationSignal(
            gps = location?.toPublisherGps(),
            wifiMacs = readWifiMacs(),
            systemLocationEnabled = isSystemLocationEnabled(),
            permissionGranted = hasLocationPermission(),
        )
    }

    override fun openLocationSettings() {
        val context = currentActivity() ?: appContext
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { qnFileLog()?.logE(TAG, "openLocationSettings failed", it) }
    }

    override fun openPermissionSettings() {
        val context = currentActivity() ?: appContext
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
            .onFailure { qnFileLog()?.logE(TAG, "openPermissionSettings failed", it) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitCurrentLocation(timeoutMs: Long): Location? {
        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null
        bestLastKnownLocation(manager)?.let { return it }
        val provider = chooseProvider(manager) ?: return null
        return withTimeoutOrNull(timeoutMs.coerceAtLeast(1_000L)) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                    }

                    @Deprecated("Deprecated in Android")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                    override fun onProviderEnabled(provider: String) = Unit

                    override fun onProviderDisabled(provider: String) = Unit
                }
                continuation.invokeOnCancellation {
                    manager.removeUpdates(listener)
                }
                runCatching {
                    manager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                }.onFailure { error ->
                    qnFileLog()?.logE(TAG, "requestSingleUpdate failed", error)
                    manager.removeUpdates(listener)
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun bestLastKnownLocation(manager: LocationManager): Location? {
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        return providers
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    private fun chooseProvider(manager: LocationManager): String? =
        when {
            runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) ->
                LocationManager.NETWORK_PROVIDER
            runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ->
                LocationManager.GPS_PROVIDER
            else -> null
        }
}

private fun currentActivity(): Activity? =
    runCatching { LocalKmmContext.getRealContext() as? Activity }.getOrNull()

private fun hasAnyLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

private fun Location.toPublisherGps(): PublisherLocationGps =
    PublisherLocationGps(
        type = 0,
        latitude = latitude.toFloat(),
        longitude = longitude.toFloat(),
        altitude = altitude.toFloat(),
    )

private fun readWifiMacs(): List<String> {
    val result = mutableListOf<String>()
    runCatching {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return@runCatching
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val macBytes = networkInterface.hardwareAddress
            if (macBytes != null && macBytes.isNotEmpty()) {
                val mac = macBytes.joinToString(":") { byte ->
                    String.format(Locale.US, "%02X", byte.toInt() and 0xFF)
                }
                if (mac.isNotBlank() && mac != "02:00:00:00:00:00") {
                    result += mac
                }
            }
        }
    }.onFailure {
        qnFileLog()?.logW(TAG, "readWifiMacs failed: ${it.message}")
    }
    return result.distinct()
}
