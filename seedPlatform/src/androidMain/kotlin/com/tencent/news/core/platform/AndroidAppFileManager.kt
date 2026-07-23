package com.tencent.news.core.platform

import android.content.Context
import com.tencent.news.core.platform.api.FileCacheLevel
import com.tencent.news.core.platform.api.FileReadCallback
import com.tencent.news.core.platform.api.FileReadResult
import com.tencent.news.core.platform.api.FileWriteCallback
import com.tencent.news.core.platform.api.FileWriteResult
import com.tencent.news.core.platform.api.IFileManager
import java.io.File

/**
 * 注入 Android 端 IFileManager 实现。
 *
 * @param context Android 应用上下文
 */
fun setupAndroidAppFileManager(context: Context) {
    QnPlatformLogic.fileManager = AndroidAppFileManager(context)
}

/**
 * Android 端 IFileManager 实现。
 *
 * 当前主要提供 [createCacheDir] 能力，在 app 缓存目录下创建子文件夹。
 * 其他文件读写方法保留空实现，后续按需扩展。
 */
internal class AndroidAppFileManager(private val context: Context) : IFileManager {

    override fun readUserFile(fileName: String, readResult: FileReadCallback) {
        readResult.invoke(FileReadResult.Error(UnsupportedOperationException("Not implemented")))
    }

    override fun writeUserFile(fileName: String, data: String, writeResult: FileWriteCallback) {
        writeResult.invoke(FileWriteResult.Error(UnsupportedOperationException("Not implemented")))
    }

    override fun deleteUserFile(filePath: String) {
        // 空实现
    }

    override fun readFile(
        dirName: String,
        fileName: String,
        level: FileCacheLevel,
        readResult: FileReadCallback
    ) {
        readResult.invoke(FileReadResult.Error(UnsupportedOperationException("Not implemented")))
    }

    override fun writeFile(
        dirName: String,
        fileName: String,
        level: FileCacheLevel,
        data: String,
        writeResult: FileWriteCallback
    ) {
        writeResult.invoke(FileWriteResult.Error(UnsupportedOperationException("Not implemented")))
    }

    override fun findFilesPath(
        dirName: String,
        fileNamePrefix: String,
        level: FileCacheLevel,
        sortAscending: Boolean,
        onCallback: (List<String>?) -> Unit
    ) {
        onCallback.invoke(null)
    }

    override fun writeMetadata4Pdf(filePath: String, metadata: Map<String, String>?) {
        // 空实现
    }

    override fun writeMetadata4Image(filePath: String, metadata: Map<String, String>?) {
        // 空实现
    }

    override fun createCacheDir(dirName: String): String? {
        if (dirName.isBlank()) return null
        return try {
            val dir = File(context.cacheDir, dirName)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            dir.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
