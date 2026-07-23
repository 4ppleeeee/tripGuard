package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.FileCacheLevel
import com.tencent.news.core.platform.api.FileReadCallback
import com.tencent.news.core.platform.api.FileReadResult
import com.tencent.news.core.platform.api.FileWriteCallback
import com.tencent.news.core.platform.api.FileWriteResult
import com.tencent.news.core.platform.api.IFileManager
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSUserDomainMask

/**
 * 注入 iOS 端 IFileManager 实现。
 */
fun setupIOSAppFileManager() {
    QnPlatformLogic.fileManager = IOSAppFileManager()
}

/**
 * iOS 端 IFileManager 实现。
 *
 * 当前主要提供 [createCacheDir] 能力，在 app 缓存目录下创建子文件夹。
 * 其他文件读写方法保留空实现，后续按需扩展。
 */
internal class IOSAppFileManager : IFileManager {

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

    @Suppress("UNCHECKED_CAST")
    override fun createCacheDir(dirName: String): String? {
        if (dirName.isBlank()) return null
        return try {
            val cachesDir = NSSearchPathForDirectoriesInDomains(
                NSCachesDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String ?: return null
            val dirPath = "$cachesDir/$dirName"
            val fm = NSFileManager.defaultManager
            if (!fm.fileExistsAtPath(dirPath)) {
                fm.createDirectoryAtPath(
                    dirPath,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
            dirPath
        } catch (e: Exception) {
            null
        }
    }
}
