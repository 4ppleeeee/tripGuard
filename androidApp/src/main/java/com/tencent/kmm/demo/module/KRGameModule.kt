package com.tencent.kmm.demo.module

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Log
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import org.json.JSONArray
import org.json.JSONObject

/**
 * GameModule 在 Kuikly Render 模式下的 Native 端实现。
 *
 * 提供游戏下载管理能力：检查应用状态、下载/暂停/删除/安装/启动应用、
 * 获取下载列表、检查存储空间等。
 *
 * 使用方式：
 * 1. 宿主 App 在 Application.onCreate 中调用 KRGameModule.registerService(impl)
 * 2. 下载状态变化时，宿主 App 调用 KRGameModule.notifyDownloadStatus(appId, status, progress)
 *    将状态推送到 Kuikly 侧的 NotifyModule
 *
 * 通知链路：
 * - DSL 侧 GameModule.checkAppStatus() -> asyncToNativeMethod -> KRGameModule.call("checkAppStatus")
 * - KRGameModule 调用 IGameDownloadService.checkAppStatus()
 * - 客户端下载状态变化 -> KRGameModule.notifyDownloadStatus() -> sendEvent -> DSL 侧 NotifyModule
 */
class KRGameModule : KuiklyRenderBaseModule() {

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        Log.i(TAG, "call: method=$method")
        return when (method) {
            METHOD_CHECK_APP_STATUS -> checkAppStatus(params, callback)
            METHOD_DOWNLOAD_APP -> downloadApp(params, callback)
            METHOD_DELETE_DOWNLOAD_APP -> deleteDownloadApp(params, callback)
            METHOD_LAUNCH_APP -> launchApp(params, callback)
            METHOD_INSTALL_APP -> installApp(params, callback)
            METHOD_GET_DOWNLOADING_LIST -> getDownloadingList(params, callback)
            METHOD_GET_DOWNLOADED_LIST -> getDownloadedList(params, callback)
            METHOD_CHECK_GAME_STORAGE -> checkGameStorage(params, callback)
            METHOD_DELETE_DOWNLOAD_LIST -> deleteDownloadList(params, callback)
            else -> {
                Log.e(TAG, "method not found: $method")
                callback?.invoke(mapOf("code" to -1, "message" to "method not found: $method"))
                null
            }
        }
    }

    // ==================== 方法实现 ====================

    /**
     * 检查应用下载/安装状态
     * 客户端收到后会通过 NotifyModule 的 "downloadCallback" 通知回传状态
     */
    private fun checkAppStatus(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "checkAppStatus: service not registered")
            return null
        }
        try {
            val json = params?.let { JSONObject(it) } ?: return null
            service.checkAppStatus(json)
        } catch (e: Exception) {
            Log.e(TAG, "checkAppStatus error", e)
        }
        return null
    }

    /**
     * 下载/暂停/继续下载应用
     */
    private fun downloadApp(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "downloadApp: service not registered")
            return null
        }
        try {
            val json = params?.let { JSONObject(it) } ?: return null
            val appInfo = json.optJSONObject("appInfo") ?: json
            val actionCode = json.optInt("actionCode", 0)
            service.downloadApp(appInfo, actionCode)
        } catch (e: Exception) {
            Log.e(TAG, "downloadApp error", e)
        }
        return null
    }

    /**
     * 删除已下载的应用
     */
    private fun deleteDownloadApp(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "deleteDownloadApp: service not registered")
            return null
        }
        try {
            val json = params?.let { JSONObject(it) } ?: return null
            service.deleteDownloadApp(json)
        } catch (e: Exception) {
            Log.e(TAG, "deleteDownloadApp error", e)
        }
        return null
    }

    /**
     * 启动已安装的应用
     */
    private fun launchApp(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "launchApp: service not registered")
            // 降级：尝试通过 PackageManager 启动
            launchAppFallback(params)
            return null
        }
        try {
            val json = params?.let { JSONObject(it) } ?: return null
            service.launchApp(json)
        } catch (e: Exception) {
            Log.e(TAG, "launchApp error", e)
        }
        return null
    }

    /**
     * 安装已下载的应用
     */
    private fun installApp(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "installApp: service not registered")
            return null
        }
        try {
            val json = params?.let { JSONObject(it) } ?: return null
            service.installApp(json)
        } catch (e: Exception) {
            Log.e(TAG, "installApp error", e)
        }
        return null
    }

    /**
     * 获取正在下载的应用列表
     */
    private fun getDownloadingList(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "getDownloadingList: service not registered, returning empty list")
            callback?.invoke(buildListResult(JSONArray()))
            return null
        }
        try {
            service.getDownloadingList(object : IGameListCallback {
                override fun onResult(code: Int, data: JSONArray?) {
                    Log.i(TAG, "getDownloadingList result: code=$code, size=${data?.length()}")
                    if (code == 0) {
                        callback?.invoke(buildListResult(data ?: JSONArray()))
                    } else {
                        callback?.invoke(mapOf("code" to code, "data" to JSONArray().toString()))
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "getDownloadingList error", e)
            callback?.invoke(buildListResult(JSONArray()))
        }
        return null
    }

    /**
     * 获取已下载（未安装）的应用列表
     */
    private fun getDownloadedList(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "getDownloadedList: service not registered, returning empty list")
            callback?.invoke(buildListResult(JSONArray()))
            return null
        }
        try {
            service.getDownloadedList(object : IGameListCallback {
                override fun onResult(code: Int, data: JSONArray?) {
                    Log.i(TAG, "getDownloadedList result: code=$code, size=${data?.length()}")
                    if (code == 0) {
                        callback?.invoke(buildListResult(data ?: JSONArray()))
                    } else {
                        callback?.invoke(mapOf("code" to code, "data" to JSONArray().toString()))
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "getDownloadedList error", e)
            callback?.invoke(buildListResult(JSONArray()))
        }
        return null
    }

    /**
     * 检查游戏存储空间
     */
    private fun checkGameStorage(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "checkGameStorage: service not registered")
            val result = JSONObject().apply {
                put("code", 0)
                put("data", JSONObject().apply {
                    put("usedSize", 0L)
                    put("remainSize", 0L)
                })
            }
            callback?.invoke(mapOf("code" to 0, "data" to result.getJSONObject("data").toString()))
            return null
        }
        try {
            service.checkGameStorage(object : IGameStorageCallback {
                override fun onResult(code: Int, usedSize: Long, remainSize: Long) {
                    Log.i(TAG, "checkGameStorage result: code=$code, used=$usedSize, remain=$remainSize")
                    val data = JSONObject().apply {
                        put("usedSize", usedSize)
                        put("remainSize", remainSize)
                    }
                    callback?.invoke(mapOf("code" to code, "data" to data.toString()))
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "checkGameStorage error", e)
            callback?.invoke(mapOf("code" to -1, "data" to JSONObject().toString()))
        }
        return null
    }

    /**
     * 批量删除下载列表中的应用
     */
    private fun deleteDownloadList(params: String?, callback: KuiklyRenderCallback?): Any? {
        val service = gameDownloadService
        if (service == null) {
            Log.w(TAG, "deleteDownloadList: service not registered")
            callback?.invoke(mapOf("code" to -1, "message" to "service not registered"))
            return null
        }
        try {
            val json = params?.let { JSONObject(it) } ?: return null
            val downloadList = json.optJSONArray("downloadList") ?: JSONArray()
            service.deleteDownloadList(downloadList, object : IGameDeleteListCallback {
                override fun onResult(code: Int) {
                    Log.i(TAG, "deleteDownloadList result: code=$code")
                    callback?.invoke(mapOf("code" to code))
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "deleteDownloadList error", e)
            callback?.invoke(mapOf("code" to -1, "message" to (e.message ?: "unknown error")))
        }
        return null
    }

    // ==================== 工具方法 ====================

    private fun buildListResult(data: JSONArray): Map<String, Any> {
        return mapOf("code" to 0, "data" to data.toString())
    }

    /**
     * 降级方案：通过 PackageManager 启动应用
     */
    private fun launchAppFallback(params: String?) {
        try {
            val json = params?.let { JSONObject(it) } ?: return
            val packageName = json.optString("packageName")
            if (packageName.isNullOrEmpty()) return
            val ctx = context ?: return
            val intent = ctx.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                ctx.startActivity(intent)
                Log.i(TAG, "launchApp fallback success: $packageName")
            } else {
                Log.w(TAG, "launchApp fallback: launch intent not found for $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "launchApp fallback error", e)
        }
    }

    // ==================== Service 接口定义 ====================

    /**
     * 列表查询回调
     */
    interface IGameListCallback {
        fun onResult(code: Int, data: JSONArray?)
    }

    /**
     * 存储空间查询回调
     */
    interface IGameStorageCallback {
        fun onResult(code: Int, usedSize: Long, remainSize: Long)
    }

    /**
     * 批量删除回调
     */
    interface IGameDeleteListCallback {
        fun onResult(code: Int)
    }

    /**
     * 游戏下载管理 Service 接口
     * 由宿主 App 注入具体实现（依赖客户端下载管理 SDK）
     *
     * 注册方式：
     * ```
     * KRGameModule.registerService(object : KRGameModule.IGameDownloadService {
     *     override fun checkAppStatus(appInfo: JSONObject) { ... }
     *     override fun downloadApp(appInfo: JSONObject, actionCode: Int) { ... }
     *     ...
     * })
     * ```
     *
     * 下载状态通知：
     * 当下载状态变化时，宿主 App 需要调用：
     * ```
     * KRGameModule.notifyDownloadStatus(appId, status, progress)
     * ```
     * 该方法会通过 sendEvent 将通知推送到 Kuikly 侧的 NotifyModule
     */
    interface IGameDownloadService {
        /**
         * 检查应用下载/安装状态
         * 实现方应在状态确定后调用 KRGameModule.notifyDownloadStatus() 回传状态
         *
         * @param appInfo 应用信息 JSON，包含 gameId、packageName、appId 等字段
         */
        fun checkAppStatus(appInfo: JSONObject)

        /**
         * 下载/暂停/继续下载应用
         *
         * @param appInfo 应用信息 JSON
         * @param actionCode 操作码：0=下载, 1=暂停, 2=继续
         */
        fun downloadApp(appInfo: JSONObject, actionCode: Int)

        /**
         * 删除已下载的应用文件
         *
         * @param appInfo 应用信息 JSON
         */
        fun deleteDownloadApp(appInfo: JSONObject)

        /**
         * 启动已安装的应用
         *
         * @param appInfo 应用信息 JSON，需包含 packageName
         */
        fun launchApp(appInfo: JSONObject)

        /**
         * 安装已下载的应用
         *
         * @param appInfo 应用信息 JSON
         */
        fun installApp(appInfo: JSONObject)

        /**
         * 获取正在下载的应用列表
         *
         * @param callback 结果回调，data 为 JSONArray，每个元素包含 AppDownloadInfo 字段
         */
        fun getDownloadingList(callback: IGameListCallback)

        /**
         * 获取已下载（未安装）的应用列表
         *
         * @param callback 结果回调，data 为 JSONArray，每个元素包含 AppDownloadInfo 字段
         */
        fun getDownloadedList(callback: IGameListCallback)

        /**
         * 检查游戏存储空间
         *
         * @param callback 结果回调，返回已用空间和剩余空间（单位：字节）
         */
        fun checkGameStorage(callback: IGameStorageCallback)

        /**
         * 批量删除下载列表中的应用
         *
         * @param downloadList 要删除的应用列表 JSONArray
         * @param callback 结果回调
         */
        fun deleteDownloadList(downloadList: JSONArray, callback: IGameDeleteListCallback)
    }

    companion object {
        const val MODULE_NAME = "GameModule"
        private const val TAG = "KRGameModule"

        // Method 常量
        private const val METHOD_CHECK_APP_STATUS = "checkAppStatus"
        private const val METHOD_DOWNLOAD_APP = "downloadApp"
        private const val METHOD_DELETE_DOWNLOAD_APP = "deleteDownloadApp"
        private const val METHOD_LAUNCH_APP = "launchApp"
        private const val METHOD_INSTALL_APP = "installApp"
        private const val METHOD_GET_DOWNLOADING_LIST = "getDownloadingList"
        private const val METHOD_GET_DOWNLOADED_LIST = "getDownloadedList"
        private const val METHOD_CHECK_GAME_STORAGE = "checkGameStorage"
        private const val METHOD_DELETE_DOWNLOAD_LIST = "deleteDownloadList"

        // NotifyModule 事件名（与 DSL 侧 GameModule.DOWN_LOAD_CALLBACK 一致）
        private const val NOTIFY_DOWNLOAD_CALLBACK = "downloadCallback"

        // Service 实例（由宿主 App 注入）
        @Volatile
        private var gameDownloadService: IGameDownloadService? = null

        // 持有所有活跃的 KRGameModule 实例，用于发送通知
        private val activeInstances = mutableSetOf<KRGameModule>()

        /**
         * 注册游戏下载管理 Service 实现
         * 应在 Application.onCreate 或初始化阶段调用
         *
         * 示例：
         * ```
         * KRGameModule.registerService(GameDownloadServiceImpl())
         * ```
         */
        fun registerService(service: IGameDownloadService) {
            gameDownloadService = service
            Log.i(TAG, "registerService: ${service.javaClass.simpleName}")
        }

        /**
         * 通知下载状态变化
         * 宿主 App 在下载状态变化时调用此方法，将状态推送到 Kuikly 侧
         *
         * @param appId 应用的 gameId（与 DSL 侧 AppDownloadInfo.gameId 对应）
         * @param status 下载状态（如 "downloading", "complete", "paused" 等）
         * @param progress 下载进度（0.0 ~ 1.0）
         *
         * 示例：
         * ```
         * KRGameModule.notifyDownloadStatus("12345", "downloading", 0.5)
         * ```
         */
        fun notifyDownloadStatus(appId: String, status: String, progress: Double) {
            Log.i(TAG, "notifyDownloadStatus: appId=$appId, status=$status, progress=$progress")
            val data = mapOf(
                "appId" to appId,
                "status" to status,
                "progress" to progress
            )
            synchronized(activeInstances) {
                for (instance in activeInstances) {
                    try {
                        instance.sendDownloadCallback(data)
                    } catch (e: Exception) {
                        Log.e(TAG, "notifyDownloadStatus error for instance", e)
                    }
                }
            }
        }
    }

    // ==================== 生命周期管理 ====================

    init {
        synchronized(activeInstances) {
            activeInstances.add(this)
        }
        Log.d(TAG, "instance created, active count: ${activeInstances.size}")
    }

    /**
     * Module 销毁时移除实例引用
     */
    override fun onDestroy() {
        super.onDestroy()
        synchronized(activeInstances) {
            activeInstances.remove(this)
        }
        Log.d(TAG, "instance destroyed, active count: ${activeInstances.size}")
    }

    /**
     * 向 Kuikly 侧发送 downloadCallback 通知
     * 通过 Kuikly Render 框架的 sendEvent 机制触发 DSL 侧的 NotifyModule
     */
    private fun sendDownloadCallback(data: Map<String, Any>) {
        try {
            // 在 Kuikly Render 模式下，通过 Module 的 callback 机制无法主动推送通知
            // NotifyModule 的通知是由框架内部管理的
            // 这里通过保存的 callback 引用来实现通知推送
            // 实际上，在 Kuikly Render 模式下，Native 端向 DSL 侧发送通知
            // 需要通过 KuiklyRenderViewDelegator.sendEvent() 实现
            // 但 Module 内部无法直接访问 delegator
            // 因此采用 "持久回调" 模式：DSL 侧注册一个持久回调，Native 端多次调用
            downloadCallbackRef?.invoke(data)
        } catch (e: Exception) {
            Log.e(TAG, "sendDownloadCallback error", e)
        }
    }

    // 持久回调引用（由 DSL 侧通过特殊方法注册）
    private var downloadCallbackRef: KuiklyRenderCallback? = null

    /**
     * 重写 call 方法的 Any? 版本，处理注册持久回调的场景
     */
    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        if (method == "registerDownloadCallback" && callback != null) {
            Log.i(TAG, "registerDownloadCallback: callback registered")
            downloadCallbackRef = callback
            return null
        }
        return super.call(method, params, callback)
    }
}
