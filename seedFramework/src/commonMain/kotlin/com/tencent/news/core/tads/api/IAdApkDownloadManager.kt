package com.tencent.news.core.tads.api

import com.tencent.news.core.util.IDistinctListener
import kotlinx.coroutines.flow.StateFlow

interface IAdApkDownloadManager {

    fun getApkState(apkInfo: IAdApkInfo): StateFlow<IAdApkInfo>?

    /**
     * 注册
     */
    fun registerListener(apkInfo: IAdApkInfo, listener: IApkDownloadListener) {}

    /**
     * 解注册
     */
    fun unRegisterListener(apkInfo: IAdApkInfo, listener: IApkDownloadListener) {}


    /**
     * 安装应用
     */
    fun installApp(apkInfo: IAdApkInfo, triggerByUser: Boolean) {}


    /**
     * 打开应用
     */
    fun openApp(apkInfo: IAdApkInfo) {}


    fun openApp(packageName: String, scheme: String? = null) {}


    /**
     * 开始
     */
    fun start(apkInfo: IAdApkInfo): Int {
        return StartDownloadResult.RESULT_UNKNOWN
    }


    /**
     * 停止
     */
    fun delete(apkInfo: IAdApkInfo) {}

    /**
     * 暂停
     */
    fun pause(apkInfo: IAdApkInfo) {}

    /**
     * 继续
     */
    fun continueDownload(apkInfo: IAdApkInfo) {
        start(apkInfo)
    }

    /**
     * 查询下载状态
     */
    fun queryDownload(apkInfo: IAdApkInfo): IAdApkInfo? = apkInfo


    /**
     * 创建 AdApkInfo
     */
    fun createAdApkInfo(data: IDownloadOriginData): IAdApkInfo? = null


    /**
     * 打开提醒弹窗
     */
    fun openAlertDialog(apkInfo: IAdApkInfo, originData: IDownloadOriginData) {}


    fun autoMarketDownload(param: AdMarketAutoDownloadParam) {}

    fun getAllApkList(): List<IAdApkInfo>? { return null }

    fun isReadyToInstall(apkInfo: IAdApkInfo): Boolean = false
}


internal const val NUM_100 = 100

interface IAdApkInfo {
    val appId: String
    val progress: Long
    val downloadState: Int
    val fileSize: Long
    fun generateListenerKeyFromPackage(): String
}

// 取值范围：[0.0, 100.0]
fun IAdApkInfo.getProgress(): Float {
    if (fileSize <= 0) {
        return 0f
    }
    val progress = progress.toFloat() / fileSize * NUM_100
    return if (progress < 0 || progress > NUM_100) 0f else progress
}


interface IApkDownloadListener : IDistinctListener {
    fun onUpdate(apkInfo: IAdApkInfo)
    fun unRegisterWhenDelete(): Boolean = true  // 删除下载任务时，是否需要自动解注册
}

// todo jiamin 这里后续需要填充字段，不能依赖具体继承的类
interface IDownloadOriginData {
    fun enableDownload(): Boolean = true
}

/**
 * H5 JSAPI 下载等场景传入的下载原始数据（从 wesee-core 同步而来，供 WebView JSAPI 插件构造下载任务）。
 */
data class AdApkDownloadOriginData(
    val appId: String = "",
    val appName: String = "",
    val appIcon: String = "",
    val packageName: String = "",
    val versionCode: Int = 0,
    val downloadUrl: String = "",
    val fileMd5: String = "",
    val fileSize: Long = 0L,
    val downloadedSize: Long = 0L,
    val downloadState: Int = 0,
    val deepLink: String = "",
    val autoInstall: Boolean = true,
) : IDownloadOriginData


object StartDownloadResult {
    const val RESULT_UNKNOWN = -2
    const val RESULT_FAILED = -1
    const val RESULT_SUCCEED = 0
    const val RESULT_EXIST = 1
    const val RESULT_DOWNLOADING = 2
}