package com.tencent.news.core.audio.api

import com.tencent.news.core.platform.QnPlatformLogic

interface IFileCacheManager {

    /**
     * 返回文件绝对路径
     *
     * @param folderPath 文件缓存目录
     * @param fileName 文件名
     *
     */
    fun fileAbsolutePath(folderPath: String, fileName: String): String?


    /**
     * 缓存文件
     *
     * @param filePath 缓存的文件目录
     * @param data 文件数据
     *
     */
    fun cacheFile(filePath: String, data: String): Boolean


    /**
     * 判断文件是否存在
     *
     * @param filePath 文件本地路径
     *
     */
    fun containsFile(filePath: String): Boolean


    /**
     * 删除缓存文件
     *
     * @param filePath 文件本地路径
     *
     */
    fun removeFile(filePath: String)


    /**
     * 删除指定目录下的文件
     *
     * @param dirPath 目录
     *
     */
    fun removeDirPathFile(dirPath: String)

}

fun fileCacheManager(): IFileCacheManager =
    QnPlatformLogic.fileCacheManager ?: DefaultFileCacheManager()

private class DefaultFileCacheManager : IFileCacheManager {

    override fun fileAbsolutePath(folderPath: String, fileName: String): String? = null

    override fun cacheFile(filePath: String, data: String): Boolean = false

    override fun containsFile(filePath: String): Boolean = false

    override fun removeFile(filePath: String) {}

    override fun removeDirPathFile(dirPath: String) {}

}