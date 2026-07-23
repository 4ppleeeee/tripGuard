package com.tencent.news.core.ohos.setup.knoi.consumer

import com.tencent.tmm.knoi.annotation.ServiceConsumer

val ohosFileCacheService: OhosFileCacheService = getOhosFileCacheServiceApi()

/**
 * 鸿蒙端文件缓存同步服务，由 ArkTS 侧 OhosFileCacheServiceImpl 提供实现。
 *
 * 与 [OhosFileManagerService] 的区别：
 *  - [OhosFileManagerService] 是针对业务层文件读写（返回 Promise 异步）
 *  - [OhosFileCacheService] 是针对 [com.tencent.news.core.audio.api.IFileCacheManager] 的
 *    同步文件查询/写入/删除 API，底层使用 @kit.CoreFileKit 的 fileIo 同步方法（accessSync 等）
 *
 * 所有方法都是同步的，直接返回结果。
 */
@ServiceConsumer
interface OhosFileCacheService {

    /**
     * 检查文件是否存在。
     * @param filePath 文件绝对路径
     * @return true 表示文件存在
     */
    fun containsFile(filePath: String): Boolean

    /**
     * 同步写入文本内容到指定文件。
     * 如果父目录不存在会自动创建。
     * @param filePath 文件绝对路径
     * @param data 要写入的字符串内容（会覆盖原文件）
     * @return true 表示写入成功
     */
    fun cacheFile(filePath: String, data: String): Boolean

    /**
     * 删除单个文件。
     * @param filePath 文件绝对路径（不存在时静默忽略）
     */
    fun removeFile(filePath: String)

    /**
     * 删除指定目录下的所有文件（不递归删除子目录）。
     * @param dirPath 目录绝对路径（不存在时静默忽略）
     */
    fun removeDirPathFile(dirPath: String)
}
