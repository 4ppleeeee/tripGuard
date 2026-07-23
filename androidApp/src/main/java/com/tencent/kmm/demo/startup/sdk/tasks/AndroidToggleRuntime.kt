package com.tencent.kmm.demo.startup.sdk.tasks

import android.content.Context
import android.os.Build
import android.os.Process
import com.tencent.rdelivery.DependencyInjector
import com.tencent.rdelivery.RDelivery
import com.tencent.rdelivery.RDeliverySetting
import com.tencent.rdelivery.dependencyimpl.HandlerTask
import com.tencent.rdelivery.dependencyimpl.HttpsURLConnectionNetwork
import com.tencent.rdelivery.dependencyimpl.MmkvStorage
import com.tencent.rdelivery.dependencyimpl.SystemLog
import com.tencent.rdelivery.dependencyimpl.initMMKV
import com.tencent.rdelivery.listener.LocalDataInitListener
import com.tencent.rdelivery.net.BaseProto
import com.tencent.kmm.startup.std.config.ToggleInitConfig

/**
 * Android Shiply/Toggle 运行时
 */
object AndroidToggleRuntime {
    private const val DEBUG_ENV_ID = "1"
    private const val UPDATE_INTERVAL_SECONDS = 4 * 60 * 60
    private const val UPDATE_STRATEGY =
        (1 /* START_UP */ or (1 shl 1) /* PERIODIC */ or (1 shl 3) /* NETWORK_RECONNECT */)

    @Volatile
    private var rDelivery: RDelivery? = null

    private val lock = Any()
    private var currentUserId: String = ""

    fun init(
        context: Context,
        config: ToggleInitConfig,
        qimei36: String,
        userId: String,
        isMainProcess: Boolean
    ) {
        synchronized(lock) {
            if (rDelivery != null) {
                switchUserAndRefreshLocked(userId)
                return
            }

            val refreshMode = if (isMainProcess) {
                BaseProto.DataRefreshMode.FROM_SERVER
            } else {
                BaseProto.DataRefreshMode.FROM_LOCAL_STORAGE
            }

            val setting = buildSetting(
                context = context,
                config = config,
                qimei36 = qimei36,
                userId = userId,
                refreshMode = refreshMode
            )

            val mmkvRootDir = context.filesDir.absolutePath + "/mmkv"
            initMMKV(mmkvRootDir)
            val injector = DependencyInjector(
                HttpsURLConnectionNetwork(context),
                { MmkvStorage.MmkvStorageFactory().createIRStorage(it) },
                HandlerTask(),
                SystemLog()
            )
            val localDataInitListener = object : LocalDataInitListener {
                override fun onInitFinish() = Unit
            }

            rDelivery = RDelivery.create(
                context.applicationContext,
                setting,
                injector,
                localDataInitListener
            )
            currentUserId = userId
        }
    }

    fun isEnable(key: String, defaultValue: Boolean): Boolean {
        return runCatching {
            rDelivery?.isOnByKey(key, defaultValue, true) ?: defaultValue
        }.getOrDefault(defaultValue)
    }

    fun getStringValue(key: String, defaultValue: String): String {
        return runCatching {
            rDelivery?.getStringByKey(key, defaultValue, true) ?: defaultValue
        }.getOrDefault(defaultValue)
    }

    private fun buildSetting(
        context: Context,
        config: ToggleInitConfig,
        qimei36: String,
        userId: String,
        refreshMode: BaseProto.DataRefreshMode
    ): RDeliverySetting {
        return RDeliverySetting.Builder()
            .setLogicEnvironment(if (config.useTestEnv) DEBUG_ENV_ID else null)
            .setQimei(qimei36)
            .setAppId(config.appId)
            .setAppKey(config.appKey)
            .setUserId(userId)
            .setIsDebugPackage(config.isDebug)
            .setUpdateStrategy(UPDATE_STRATEGY)
            .setUpdateInterval(UPDATE_INTERVAL_SECONDS)
            .setBundleId(context.packageName)
            .setHostAppVersion(config.appVersion)
            .setDevModel(Build.MODEL)
            .setDevManufacturer(Build.MANUFACTURER)
            .setAndroidSystemVersion(Build.VERSION.SDK_INT.toString())
            .setIs64BitCpu(is64BitProcess())
            .setDataRefreshMode(refreshMode)
            .build()
    }

    private fun is64BitProcess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Process.is64Bit()
        } else {
            false
        }
    }

    fun refreshUser(userId: String) {
        synchronized(lock) {
            switchUserAndRefreshLocked(userId)
        }
    }

    private fun switchUserAndRefreshLocked(userId: String) {
        val sdk = rDelivery ?: return
        if (currentUserId != userId) {
            sdk.switchUserId(userId)
            currentUserId = userId
        }
        requestFullRemoteDataIfSupported(sdk)
    }

    private fun requestFullRemoteDataIfSupported(sdk: RDelivery) {
        // RDelivery Android SDK 历史版本的主动拉取 API 名称不稳定，这里保持反射兜底，
        // 确保账号切换后有能力刷新配置，同时不绑定某个特定 SDK 版本。
        val method = runCatching {
            sdk.javaClass.methods.firstOrNull { candidate ->
                candidate.parameterTypes.isEmpty() &&
                    candidate.name in setOf("requestFullRemoteData", "updateAllConfig", "updateConfig")
            }
        }.getOrNull() ?: return
        runCatching { method.invoke(sdk) }
    }
}
