package com.tencent.news.core.ohos.setup.knoi.consumer

import com.tencent.tmm.knoi.annotation.ServiceConsumer
import com.tencent.tmm.knoi.type.JSValue

val ohosFileManagerService: OhosFileManagerService = getOhosFileManagerServiceApi()

/**
 * 鸿蒙端文件读写服务，由 ArkTS 侧 OhosFileManagerServiceImpl 提供实现。
 *
 * - readUserFile / writeUserFile：按"是否用户隔离"由宿主自行选择目录。
 * - findFilesPath：返回 JSON 编码的 List<String>（绝对路径数组）。
 */
@ServiceConsumer
interface OhosFileManagerService {
    fun readUserFile(fileName: String, needUser: Boolean): JSValue
    fun writeUserFile(fileName: String, data: String, needUser: Boolean): JSValue
    fun deleteUserFile(filePath: String)
    fun findFilesPath(
        dirName: String,
        fileNamePrefix: String,
        sortAscending: Boolean,
    ): JSValue

    /**
     * 在 app 缓存目录下创建子文件夹，返回该文件夹的绝对路径。
     * 如果目录已存在则直接返回路径，不会重复创建。
     *
     * @param dirName 子文件夹名称（相对于 app 缓存根目录）
     * @return 创建成功返回绝对路径，失败返回空字符串
     */
    fun createCacheDir(dirName: String): String
}
