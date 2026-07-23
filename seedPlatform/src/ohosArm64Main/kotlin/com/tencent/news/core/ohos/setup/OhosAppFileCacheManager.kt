package com.tencent.news.core.ohos.setup

import com.tencent.news.core.audio.api.IFileCacheManager
import com.tencent.news.core.ohos.setup.knoi.consumer.ohosFileCacheService
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.qnLogcat

/**
 * 注入鸿蒙端 [IFileCacheManager] 实现。
 *
 * 通过 knoi 桥接到 ArkTS 侧 OhosFileCacheServiceImpl 的真实文件操作实现：
 *  - [fileAbsolutePath] : 在 Kotlin 侧拼接 "folderPath/fileName"（纯字符串拼接，无需桥接）
 *  - [cacheFile]        : 通过 [ohosFileCacheService].cacheFile 同步写入文件，自动创建父目录
 *  - [containsFile]     : 通过 fileIo.accessSync 同步判断文件是否存在
 *  - [removeFile]       : 通过 fileIo.unlinkSync 同步删除单个文件
 *  - [removeDirPathFile]: 通过 fileIo.listFileSync + unlinkSync 同步清空目录下所有文件
 *
 * 与 [OhosAppFileManager]（面向业务的异步读写）的区别：此实现专注于音频/本地缓存场景的
 * 同步查询/删除，路径由调用方传入绝对路径（通常为 filesDir / cacheDir 下的子目录）。
 */
fun setupOhosAppFileCacheManager() {
    QnPlatformLogic.fileCacheManager = OhosAppFileCacheManager
}

private object OhosAppFileCacheManager : IFileCacheManager {

    private const val TAG = "OhosAppFileCacheManager"

    override fun fileAbsolutePath(folderPath: String, fileName: String): String? {
        if (folderPath.isEmpty() || fileName.isEmpty()) {
            return null
        }
        // 避免出现双斜杠
        val normalizedFolder = folderPath.trimEnd('/')
        val normalizedFile = fileName.trimStart('/')
        return "$normalizedFolder/$normalizedFile"
    }

    override fun cacheFile(filePath: String, data: String): Boolean {
        if (filePath.isEmpty()) return false
        return runCatching { ohosFileCacheService.cacheFile(filePath, data) }
            .onFailure { qnLogcat()?.logE(TAG, "cacheFile failed: $filePath, err=$it") }
            .getOrDefault(false)
    }

    override fun containsFile(filePath: String): Boolean {
        if (filePath.isEmpty()) return false
        return runCatching { ohosFileCacheService.containsFile(filePath) }
            .onFailure { qnLogcat()?.logE(TAG, "containsFile failed: $filePath, err=$it") }
            .getOrDefault(false)
    }

    override fun removeFile(filePath: String) {
        if (filePath.isEmpty()) return
        runCatching { ohosFileCacheService.removeFile(filePath) }
            .onFailure { qnLogcat()?.logE(TAG, "removeFile failed: $filePath, err=$it") }
    }

    override fun removeDirPathFile(dirPath: String) {
        if (dirPath.isEmpty()) return
        runCatching { ohosFileCacheService.removeDirPathFile(dirPath) }
            .onFailure { qnLogcat()?.logE(TAG, "removeDirPathFile failed: $dirPath, err=$it") }
    }
}
