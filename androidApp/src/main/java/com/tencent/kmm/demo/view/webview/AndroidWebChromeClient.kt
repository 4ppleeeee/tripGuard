package com.tencent.kmm.demo.view.webview

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kmm.demo.library.log.WsLogger
import org.json.JSONObject

/**
 * 自定义 WebChromeClient
 * 处理页面标题变化、加载进度等回调
 */
class AndroidWebChromeClient(private val hostContext: Context? = null) : WebChromeClient() {

    companion object {
        private const val TAG = "AndroidWebChromeClient"
        private const val REQUEST_CODE_FILE_CHOOSER = 0x57E1
        private const val REQUEST_CODE_MEDIA_PERMISSION = 0x57E2

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
            return AndroidWebViewFileChooser.handleActivityResult(requestCode, resultCode, data)
        }

        fun handleRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ): Boolean {
            return AndroidWebViewMediaPermission.handleRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    /** 收到页面标题回调 */
    var onReceiveTitleCallback: KuiklyRenderCallback? = null

    /** 加载进度变化回调 */
    var onProgressChangedCallback: KuiklyRenderCallback? = null

    /** 是否为未成年人模式重置密码实名认证 WebView。 */
    var teenResetAuthScene: Boolean = false

    /** 复用的 JSONObject，避免高频回调中反复创建（onProgressChanged 每秒可能触发数十次） */
    private val reusableProgressJson = JSONObject()
    private val reusableTitleJson = JSONObject()

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        onReceiveTitleCallback?.let { callback ->
            reusableTitleJson.put("title", title ?: "")
            callback.invoke(reusableTitleJson)
        }
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressChangedCallback?.let { callback ->
            reusableProgressJson.put("progress", newProgress)
            callback.invoke(reusableProgressJson)
        }
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Boolean {
        return AndroidWebViewFileChooser.open(webView, filePathCallback, fileChooserParams)
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        AndroidWebViewMediaPermission.open(hostContext?.findActivity(), request, teenResetAuthScene)
    }

    override fun onPermissionRequestCanceled(request: PermissionRequest?) {
        AndroidWebViewMediaPermission.cancel(request)
    }

    private object AndroidWebViewFileChooser {
        private var pendingCallback: ValueCallback<Array<Uri>>? = null

        fun open(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: WebChromeClient.FileChooserParams?
        ): Boolean {
            val activity = webView?.context?.findActivity()
            if (activity == null || filePathCallback == null) {
                WsLogger.e(TAG, "打开 H5 文件选择器失败：Activity 或回调为空")
                return false
            }

            pendingCallback?.onReceiveValue(null)
            pendingCallback = filePathCallback

            val intent = createFileChooserIntent(fileChooserParams)
            return try {
                activity.startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER)
                WsLogger.i(
                    TAG,
                    "已打开 H5 文件选择器 acceptTypes=${fileChooserParams?.acceptTypes?.joinToString().orEmpty()} " +
                        "mode=${fileChooserParams?.mode ?: -1} capture=${fileChooserParams?.isCaptureEnabled ?: false}"
                )
                true
            } catch (e: ActivityNotFoundException) {
                WsLogger.e(TAG, "打开 H5 文件选择器失败：没有可处理的系统选择器", e)
                clearPendingCallback()
                false
            } catch (e: Exception) {
                WsLogger.e(TAG, "打开 H5 文件选择器异常", e)
                clearPendingCallback()
                false
            }
        }

        fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
            if (requestCode != REQUEST_CODE_FILE_CHOOSER) {
                return false
            }
            val callback = pendingCallback ?: return true
            pendingCallback = null

            val uris = parseSelectedUris(resultCode, data)
            WsLogger.i(
                TAG,
                "收到 H5 文件选择结果 resultCode=$resultCode uriCount=${uris?.size ?: 0} " +
                    "hasDataUri=${data?.data != null} clipCount=${data?.clipData?.itemCount ?: 0}"
            )
            callback.onReceiveValue(uris)
            return true
        }

        private fun createFileChooserIntent(fileChooserParams: WebChromeClient.FileChooserParams?): Intent {
            if (fileChooserParams?.isCaptureEnabled == true) {
                runCatching { fileChooserParams.createIntent() }.getOrNull()?.let { return it }
            }
            val intent = createFallbackIntent(fileChooserParams)
            return Intent.createChooser(intent, "选择文件")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        private fun createFallbackIntent(fileChooserParams: WebChromeClient.FileChooserParams?): Intent {
            val acceptTypes = fileChooserParams.normalizedAcceptTypes()
            return Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = when (acceptTypes.size) {
                    0 -> "*/*"
                    1 -> acceptTypes.first()
                    else -> {
                        if (acceptTypes.size <= MAX_MIME_TYPE_FILTER_COUNT) {
                            putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes)
                        }
                        "*/*"
                    }
                }
                putExtra(
                    Intent.EXTRA_ALLOW_MULTIPLE,
                    fileChooserParams?.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE
                )
            }
        }

        private fun parseSelectedUris(resultCode: Int, data: Intent?): Array<Uri>? {
            if (resultCode != Activity.RESULT_OK) {
                return null
            }
            val parsedUris = runCatching {
                WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            }.getOrNull()
            if (!parsedUris.isNullOrEmpty()) {
                return parsedUris
            }
            val manualUris = mutableListOf<Uri>()
            data?.data?.let { manualUris.add(it) }
            val clipData = data?.clipData
            if (clipData != null) {
                for (index in 0 until clipData.itemCount) {
                    clipData.getItemAt(index)?.uri?.let { manualUris.add(it) }
                }
            }
            return manualUris.distinct().takeIf { it.isNotEmpty() }?.toTypedArray()
        }

        private fun WebChromeClient.FileChooserParams?.normalizedAcceptTypes(): Array<String> {
            val acceptTypes: List<String> = this?.acceptTypes
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() && it != "*/*" && it.contains("/") }
                ?.distinct()
                .orEmpty()
            return acceptTypes.toTypedArray()
        }

        private fun clearPendingCallback() {
            pendingCallback?.onReceiveValue(null)
            pendingCallback = null
        }

        private const val MAX_MIME_TYPE_FILTER_COUNT = 10
    }

    private object AndroidWebViewMediaPermission {
        private val supportedResources = setOf(
            PermissionRequest.RESOURCE_VIDEO_CAPTURE,
            PermissionRequest.RESOURCE_AUDIO_CAPTURE
        )
        private var pendingRequest: PermissionRequest? = null
        private var pendingResources: Array<String> = emptyArray()
        private var pendingAlreadyGrantedPermissions: Set<String> = emptySet()

        fun open(activity: Activity?, request: PermissionRequest?, teenResetAuthScene: Boolean) {
            val requestedResources = request?.resources.orEmpty()
            val grantableResources = requestedResources
                .filter { it in supportedResources }
                .toTypedArray()

            if (activity == null || request == null || grantableResources.isEmpty()) {
                WsLogger.e(
                    TAG,
                    "拒绝 H5 媒体权限：Activity、请求或可授权资源为空 requested=${requestedResources.joinToString()}"
                )
                request?.deny()
                return
            }

            if (!request.isTrustedWebMediaOrigin(teenResetAuthScene)) {
                WsLogger.e(
                    TAG,
                    "拒绝 H5 媒体权限：未命中可信域名 " +
                        "scene=$teenResetAuthScene origin=${request.origin}"
                )
                request.deny()
                return
            }

            val requiredPermissions = grantableResources
                .flatMap { it.toAndroidPermissions() }
                .distinct()
                .toTypedArray()
            val missingPermissions = requiredPermissions
                .filter {
                    ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
                }
                .toTypedArray()

            if (missingPermissions.isEmpty()) {
                WsLogger.i(
                    TAG,
                    "直接授权 H5 媒体权限 resources=${grantableResources.joinToString()} " +
                        "origin=${request.origin}"
                )
                request.grant(grantableResources)
                return
            }

            pendingRequest?.deny()
            pendingRequest = request
            pendingResources = grantableResources
            pendingAlreadyGrantedPermissions = requiredPermissions
                .filter { permission -> permission !in missingPermissions }
                .toSet()
            WsLogger.i(
                TAG,
                "请求 H5 媒体系统权限 permissions=${missingPermissions.joinToString()} " +
                    "resources=${grantableResources.joinToString()} origin=${request.origin}"
            )
            ActivityCompat.requestPermissions(activity, missingPermissions, REQUEST_CODE_MEDIA_PERMISSION)
        }

        fun handleRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ): Boolean {
            if (requestCode != REQUEST_CODE_MEDIA_PERMISSION) {
                return false
            }

            val request = pendingRequest ?: return true
            val resources = pendingResources
            val alreadyGrantedPermissions = pendingAlreadyGrantedPermissions
            pendingRequest = null
            pendingResources = emptyArray()
            pendingAlreadyGrantedPermissions = emptySet()

            val grantedPermissions = permissions
                .zip(grantResults.toTypedArray())
                .filter { (_, result) -> result == PackageManager.PERMISSION_GRANTED }
                .map { (permission, _) -> permission }
                .toSet() + alreadyGrantedPermissions
            val grantedResources = resources.filter { resource ->
                resource.toAndroidPermissions().all { permission -> permission in grantedPermissions }
            }.toTypedArray()

            if (grantedResources.isNotEmpty()) {
                WsLogger.i(
                    TAG,
                    "授权 H5 媒体权限成功 resources=${grantedResources.joinToString()} " +
                        "permissions=${grantedPermissions.joinToString()}"
                )
                request.grant(grantedResources)
            } else {
                WsLogger.e(
                    TAG,
                    "授权 H5 媒体权限失败：用户未授权 permissions=${permissions.joinToString()}"
                )
                request.deny()
            }
            return true
        }

        fun cancel(request: PermissionRequest?) {
            if (request != null && pendingRequest == request) {
                WsLogger.i(TAG, "H5 媒体权限请求已取消 origin=${request.origin}")
                pendingRequest = null
                pendingResources = emptyArray()
                pendingAlreadyGrantedPermissions = emptySet()
            }
        }

        private fun String.toAndroidPermissions(): List<String> {
            return when (this) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> listOf(Manifest.permission.CAMERA)
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> listOf(Manifest.permission.RECORD_AUDIO)
                else -> emptyList()
            }
        }

        private fun PermissionRequest.isTrustedTeenResetAuthOrigin(): Boolean {
            val host = origin?.host?.lowercase().orEmpty()
            return TRUSTED_TEEN_RESET_AUTH_HOSTS.any { trustedHost ->
                host == trustedHost || host.endsWith(".$trustedHost")
            }
        }

        private fun PermissionRequest.isTrustedDclFeedbackOrigin(): Boolean {
            val host = origin?.host?.lowercase().orEmpty()
            return host == TRUSTED_DCL_FEEDBACK_HOST
        }

        private fun PermissionRequest.isTrustedWebMediaOrigin(teenResetAuthScene: Boolean): Boolean {
            return isTrustedDclFeedbackOrigin() || (teenResetAuthScene && isTrustedTeenResetAuthOrigin())
        }

        private val TRUSTED_TEEN_RESET_AUTH_HOSTS = setOf("webank.com", "faceid.qq.com")
        private const val TRUSTED_DCL_FEEDBACK_HOST = "h5.dcl.qq.com"
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        if (consoleMessage != null) {
            WsLogger.i(
                TAG,
                "console level=${consoleMessage.messageLevel()}, source=${consoleMessage.sourceId()}:${
                    consoleMessage.lineNumber()
                }, message=${consoleMessage.message()}"
            )
        }
        return super.onConsoleMessage(consoleMessage)
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
