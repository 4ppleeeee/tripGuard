package com.tencent.kmm.demo.setup

import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import com.squareup.picasso.Picasso
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.getRealContext
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.IResManager
import com.tencent.news.core.platform.api.PaletteParam
import com.tencent.kmm.demo.KRApplication
import com.tencent.kmm.demo.library.log.WsLogger
import com.tencent.kmm.demo.utils.insertVideoToAlbum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URLConnection

private const val TAG = "AndroidResManager"

/** 系统相册多选图片的 requestCode */
private const val REQUEST_CODE_SELECT_IMAGE = 0x7E01

/** 全局回调 map，key 为 requestCode，value 为选图结果回调 */
private val selectImageCallbacks = mutableMapOf<Int, (List<String>) -> Unit>()

@KmmInternalApi
internal fun setupAndroidResManager() {
    QnPlatformLogic.resManager = AndroidResManager
}

/**
 * 处理系统相册选图的 onActivityResult 回调。
 *
 * 需要在宿主 Activity 的 [Activity.onActivityResult] 中调用此方法，
 * 将选图结果转发给 [AndroidResManager.selectImage] 的回调。
 */
fun handleSelectImageActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    val callback = selectImageCallbacks.remove(requestCode) ?: return
    if (resultCode != Activity.RESULT_OK || data == null) {
        callback(emptyList())
        return
    }
    // 收集所有待处理的 URI
    val uris = mutableListOf<Uri>()
    val clipData = data.clipData
    if (clipData != null) {
        for (i in 0 until clipData.itemCount) {
            uris.add(clipData.getItemAt(i).uri)
        }
    } else {
        data.data?.let { uris.add(it) }
    }
    // copyUriToCache 是同步 IO，必须在 IO 线程执行，避免主线程 ANR
    CoroutineScope(Dispatchers.IO).launch {
        val paths = uris.mapNotNull { uri -> copyUriToCache(uri) }
        withContext(Dispatchers.Main) {
            callback(paths)
        }
    }
}

/**
 * 将 content:// URI 复制到应用私有缓存目录，返回可访问的本地文件路径。
 *
 * Android 10+ 分区存储禁止直接通过文件路径访问 DCIM 等媒体目录（EACCES），
 * 必须通过 ContentResolver.openInputStream 读取后复制到私有目录。
 */
private fun copyUriToCache(uri: Uri): String? {
    val context = KRApplication.application
    return runCatching {
        val cacheDir = File(context.cacheDir, "report_images").also { it.mkdirs() }
        val fileName =
            "img_${System.currentTimeMillis()}_${uri.lastPathSegment?.takeLast(16) ?: "unknown"}.jpg"
        val destFile = File(cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    }.onFailure { e ->
        WsLogger.e(TAG, "copyUriToCache failed: $uri", e)
    }.getOrNull()
}

private object AndroidResManager : IResManager {

    private val app get() = KRApplication.application

    override fun getAssetJson(fileName: String): String {
        return runCatching {
            app.assets.open(fileName).use { inputStream ->
                String(inputStream.readBytes())
            }
        }.onFailure { error ->
            WsLogger.e(TAG, "读取 asset 文件失败: $fileName", error)
        }.getOrDefault("")
    }

    override fun preloadImage(
        url: String,
        onSuccess: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                Picasso.get().load(url).get()
            }.onSuccess {
                withContext(Dispatchers.Main) { onSuccess?.invoke() }
            }.onFailure {
                WsLogger.e(TAG, "预加载图片失败: $url", it)
                withContext(Dispatchers.Main) { onFail?.invoke() }
            }
        }
    }

    override fun copyToClipboard(content: String) {
        val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText(TAG, content))
    }

    override fun saveImage(url: String, metadata: Map<String, String>?) {
        if (url.isBlank()) return
        val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            ?: return
        runCatching {
            val mimeType = URLConnection.guessContentTypeFromName(url) ?: "image/*"
            val fileName = URLUtil.guessFileName(url, null, mimeType)
                .ifBlank { "kmm_${System.currentTimeMillis()}.jpg" }
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle(fileName)
                setDescription("保存图片")
                setMimeType(mimeType)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName)
            }
            downloadManager.enqueue(request)
        }.onFailure { error ->
            WsLogger.e(TAG, "保存图片失败: $url", error)
        }
    }

    override fun saveVideo(localFilePath: String): Boolean {
        return insertVideoToAlbum(app, localFilePath, "kmm_")
    }

    override fun selectImage(context: IKmmContext?, callback: (url: List<String>) -> Unit) {
        val activity = context?.getRealContext() as? Activity
        if (activity == null) {
            WsLogger.e(TAG, "selectImage failed: context is not an Activity")
            callback(emptyList())
            return
        }
        // 使用 ACTION_GET_CONTENT + EXTRA_ALLOW_MULTIPLE 打开系统相册多选
        // ACTION_PICK + MediaStore.Images.Media.EXTERNAL_CONTENT_URI 在部分机型上不支持多选
        // ACTION_GET_CONTENT 兼容性更好，且 type = "image/*" 会优先调起相册而非文件管理器
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        selectImageCallbacks[REQUEST_CODE_SELECT_IMAGE] = callback
        runCatching {
            activity.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }.onFailure { e ->
            WsLogger.e(TAG, "selectImage startActivityForResult failed", e)
            selectImageCallbacks.remove(REQUEST_CODE_SELECT_IMAGE)
            callback(emptyList())
        }
    }

    override fun getPaletteColor(
        imageUrl: String,
        param: PaletteParam,
        defaultColor: Int?,
        onGot: (color: Int) -> Unit
    ) {
        // todo genesisli dev: 需要实现取色功能
        defaultColor?.let { onGot(it) }
    }

    override fun preloadLottieToMemory(
        context: IKmmContext?,
        url: String,
        status: String,
        isDay: Boolean
    ) {
        // todo genesisli dev: 需要实现 Lottie 预加载到内存功能
        WsLogger.i(TAG, "preloadLottieToMemory: url=$url, status=$status, isDay=$isDay")
    }

    override fun preloadAlphaVideo(
        url: String,
        onSuccess: (() -> Unit)?,
        onFail: (() -> Unit)?
    ) {
        // todo genesisli dev: 需要实现 Alpha 视频预加载功能
        WsLogger.i(TAG, "preloadAlphaVideo: url=$url")
        onFail?.invoke()
    }
}
