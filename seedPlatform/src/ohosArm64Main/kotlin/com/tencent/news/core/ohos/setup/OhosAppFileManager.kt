package com.tencent.news.core.ohos.setup

import com.tencent.news.core.extension.safeDecode
import com.tencent.news.core.ohos.setup.knoi.consumer.ohosFileManagerService
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.api.FileCacheLevel
import com.tencent.news.core.platform.api.FileReadCallback
import com.tencent.news.core.platform.api.FileReadResult
import com.tencent.news.core.platform.api.FileWriteCallback
import com.tencent.news.core.platform.api.FileWriteResult
import com.tencent.news.core.platform.api.IFileManager
import com.tencent.news.core.serializer.KtJson
import com.tencent.tmm.knoi.type.asPromise

/**
 * 注入鸿蒙端 IFileManager 实现。
 *
 * 通过 knoi 桥接到 ArkTS 侧 OhosFileManagerService 的真实文件读写实现：
 *   - readUserFile / writeUserFile：按用户隔离（needUser=true），由宿主决定具体目录。
 *   - readFile / writeFile：按目录隔离（needUser=false），路径为 "$dirName/$fileName"。
 *   - findFilesPath：返回 JSON 编码的路径数组。
 *   - writeMetadata4Pdf / writeMetadata4Image：当前无业务诉求，保留空实现（与 iOS/QnCore 鸿蒙端一致）。
 *
 * 说明：FileCacheLevel 当前未透传到 ArkTS 侧；如后续需要区分
 * USERCACHE / VERSION / PERSISTENT，可扩展 OhosFileManagerService 签名并在宿主映射到不同根目录。
 */
fun setupOhosAppFileManager() {
    QnPlatformLogic.fileManager = OhosAppFileManager()
}

internal class OhosAppFileManager : IFileManager {

    // region 用户隔离文件
    override fun readUserFile(fileName: String, readResult: FileReadCallback) {
        readFileInternal(fileName = fileName, needUser = true, readResult = readResult)
    }

    override fun writeUserFile(fileName: String, data: String, writeResult: FileWriteCallback) {
        writeFileInternal(
            fileName = fileName,
            data = data,
            needUser = true,
            writeResult = writeResult,
        )
    }

    override fun deleteUserFile(filePath: String) {
        ohosFileManagerService.deleteUserFile(filePath)
    }
    // endregion

    // region 文件夹级文件
    override fun readFile(
        dirName: String,
        fileName: String,
        level: FileCacheLevel,
        readResult: FileReadCallback,
    ) {
        readFileInternal(
            fileName = "$dirName/$fileName",
            needUser = false,
            readResult = readResult,
        )
    }

    override fun writeFile(
        dirName: String,
        fileName: String,
        level: FileCacheLevel,
        data: String,
        writeResult: FileWriteCallback,
    ) {
        writeFileInternal(
            fileName = "$dirName/$fileName",
            data = data,
            needUser = false,
            writeResult = writeResult,
        )
    }

    override fun findFilesPath(
        dirName: String,
        fileNamePrefix: String,
        level: FileCacheLevel,
        sortAscending: Boolean,
        onCallback: (List<String>?) -> Unit,
    ) {
        ohosFileManagerService.findFilesPath(dirName, fileNamePrefix, sortAscending).asPromise()
            .then { result ->
                val list = if (result.isNotEmpty() && result[0].isString()) {
                    KtJson.safeDecode<List<String>>(result[0].toKString() ?: "")
                } else {
                    null
                }
                onCallback.invoke(list)
            }.catch {
                onCallback.invoke(null)
            }
    }
    // endregion

    // region metadata 写入：鸿蒙端暂无业务诉求，保留空实现（与 iOS/QnCore 鸿蒙端一致）
    override fun writeMetadata4Pdf(filePath: String, metadata: Map<String, String>?) {
        // 鸿蒙端暂未实现 PDF metadata 写入
    }

    override fun writeMetadata4Image(filePath: String, metadata: Map<String, String>?) {
        // 鸿蒙端暂未实现 Image metadata 写入
    }
    // endregion

    // region 缓存目录创建
    override fun createCacheDir(dirName: String): String? {
        if (dirName.isBlank()) return null
        return runCatching {
            val result = ohosFileManagerService.createCacheDir(dirName)
            result.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }
    // endregion

    // region 内部实现
    private fun readFileInternal(
        fileName: String,
        needUser: Boolean,
        readResult: FileReadCallback,
    ) {
        ohosFileManagerService.readUserFile(fileName, needUser).asPromise().then { data ->
            if (data.isNotEmpty() && data[0].isString()) {
                val response = data[0].toKString() ?: ""
                readResult.invoke(FileReadResult.Success(response))
            } else {
                readResult.invoke(FileReadResult.Error(Throwable("empty response")))
            }
        }.catch { error ->
            readResult.invoke(FileReadResult.Error(Throwable(error?.toString().orEmpty())))
        }
    }

    private fun writeFileInternal(
        fileName: String,
        data: String,
        needUser: Boolean,
        writeResult: FileWriteCallback,
    ) {
        ohosFileManagerService.writeUserFile(fileName, data, needUser).asPromise().then { result ->
            // ArkTS 侧约定：成功时 resolve "ok"，失败时 resolve ""（空字符串）。
            // 必须明确校验返回值为 "ok"，避免将失败的空字符串误判为成功。
            val resultStr = if (result.isNotEmpty() && result[0].isString()) result[0].toKString() else null
            if (resultStr == "ok") {
                writeResult.invoke(FileWriteResult.Success())
            } else {
                writeResult.invoke(FileWriteResult.Error(Throwable("write failed, response=$resultStr")))
            }
        }.catch { error ->
            writeResult.invoke(FileWriteResult.Error(Throwable(error?.toString().orEmpty())))
        }
    }
    // endregion
}