package com.tencent.news.core.platform.api

import com.tencent.news.core.platform.QnPlatformLogic


sealed class FileReadResult {
    data class Success(val data: String) : FileReadResult()
    data class Error(val exception: Throwable) : FileReadResult()
}


sealed class FileWriteResult {
    class Success() : FileWriteResult()
    data class Error(val exception: Throwable) : FileWriteResult()
}


enum class FileCacheLevel {
    USERCACHE,      // 用户可清理级别
    VERSION,        // 版本级别，版本升级时候会清理
    PERSISTENT,     // 永久磁盘，慎用，需要注意清理逻辑
}


typealias FileReadCallback = (FileReadResult) -> Unit
typealias FileWriteCallback = (FileWriteResult) -> Unit


interface IFileManager {
    // 按用户隔离的文件存储：由宿主负责按用户隔离文件存储
    fun readUserFile(fileName: String, readResult: FileReadCallback)
    fun writeUserFile(fileName: String, data: String, writeResult: FileWriteCallback)
    fun deleteUserFile(filePath: String)

    // 按照文件夹存储的文件
    fun readFile(
        dirName: String,
        fileName: String,
        level: FileCacheLevel,
        readResult: FileReadCallback
    )
    fun writeFile(
        dirName: String,
        fileName: String,
        level: FileCacheLevel,
        data: String,
        writeResult: FileWriteCallback
    )
    fun findFilesPath(
        dirName: String,
        fileNamePrefix: String,
        level: FileCacheLevel,
        sortAscending: Boolean,
        onCallback: (List<String>?) -> Unit
    )

    fun writeMetadata4Pdf(
        filePath: String,
        metadata: Map<String, String>?
    )

    fun writeMetadata4Image(
        filePath: String,
        metadata: Map<String, String>?
    )
    // 后续有其他文件存储需求还可以扩展，例如：app升级后清理、定时/定尺寸清理 等等
}


fun appFile(): IFileManager? {
    return QnPlatformLogic.fileManager
}