package com.tencent.news.core.ohos.setup

import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.OhosFileCacheManager

/**
 * 设置鸿蒙端文件缓存管理器
 * 在鸿蒙端应用启动时调用此函数进行注册
 */
fun setupOhosFileCacheManager() {
    QnPlatformLogic.fileCacheManager = OhosFileCacheManager()
}

/**
 * 鸿蒙端文件缓存管理器初始化器
 * 提供统一的初始化接口
 */
object OhosFileCacheManagerInitializer {
    
    /**
     * 初始化文件缓存管理器
     * 此方法应在鸿蒙端应用启动时调用
     */
    fun initialize() {
        setupOhosFileCacheManager()
    }
    
    /**
     * 检查文件缓存管理器是否已初始化
     */
    fun isInitialized(): Boolean {
        return QnPlatformLogic.fileCacheManager != null
    }
}