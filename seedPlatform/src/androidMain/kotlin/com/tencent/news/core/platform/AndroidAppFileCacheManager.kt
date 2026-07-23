package com.tencent.news.core.platform

import android.content.Context
import android.util.Base64
import android.util.Log
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.audio.api.IFileCacheManager
import java.io.File

private const val FILE_CACHE_TAG = "AndroidAppFileCacheManager"

@KmmInternalApi
internal fun setupAndroidFileCacheManager(context: Context) {
    QnPlatformLogic.fileCacheManager = AndroidAppFileCacheManager(context.applicationContext)
}

private class AndroidAppFileCacheManager(
    private val context: Context,
) : IFileCacheManager {

    override fun fileAbsolutePath(folderPath: String, fileName: String): String? {
        val normalizedFolder = folderPath.trim('/')
        val normalizedFileName = fileName.trim('/')
        if (normalizedFolder.isBlank() || normalizedFileName.isBlank()) {
            return null
        }
        return File(File(context.cacheDir, normalizedFolder), normalizedFileName).absolutePath
    }

    override fun cacheFile(filePath: String, data: String): Boolean {
        if (filePath.isBlank() || data.isBlank()) {
            return false
        }
        return runCatching {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeBytes(Base64.decode(data, Base64.DEFAULT))
            true
        }.onFailure { error ->
            Log.e(FILE_CACHE_TAG, "cacheFile failed, filePath=$filePath", error)
        }.getOrDefault(false)
    }

    override fun containsFile(filePath: String): Boolean {
        if (filePath.isBlank()) {
            return false
        }
        return File(filePath).exists()
    }

    override fun removeFile(filePath: String) {
        if (filePath.isBlank()) {
            return
        }
        runCatching {
            val file = File(filePath)
            if (file.isFile && file.exists()) {
                file.delete()
            }
        }.onFailure { error ->
            Log.e(FILE_CACHE_TAG, "removeFile failed, filePath=$filePath", error)
        }
    }

    override fun removeDirPathFile(dirPath: String) {
        if (dirPath.isBlank()) {
            return
        }
        runCatching {
            val dir = File(dirPath)
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        }.onFailure { error ->
            Log.e(FILE_CACHE_TAG, "removeDirPathFile failed, dirPath=$dirPath", error)
        }
    }
}
