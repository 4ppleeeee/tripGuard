package com.tencent.news.core.platform

import com.tencent.news.core.audio.api.IFileCacheManager
import com.tencent.news.core.platform.QnPlatformLogic

/**
 * 设置鸿蒙端文件缓存管理器
 */
fun setupFileCacheManager() {
    QnPlatformLogic.fileCacheManager = OhosFileCacheManager()
}

/**
 * 鸿蒙端文件缓存管理器实现
 */
class OhosFileCacheManager : IFileCacheManager {

    override fun fileAbsolutePath(folderPath: String, fileName: String): String? {
        // TODO: 鸿蒙端实现 - 返回文件绝对路径
        return null
    }

    override fun cacheFile(filePath: String, data: String): Boolean {
        // TODO: 鸿蒙端实现 - 缓存文件
        return false
    }

    override fun containsFile(filePath: String): Boolean {
        // TODO: 鸿蒙端实现 - 判断文件是否存在
        return false
    }

    override fun removeFile(filePath: String) {
        // TODO: 鸿蒙端实现 - 删除缓存文件
    }

    override fun removeDirPathFile(dirPath: String) {
        // TODO: 鸿蒙端实现 - 删除指定目录下的文件
    }
}