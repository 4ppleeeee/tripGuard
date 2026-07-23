@file:Suppress("unused")

package com.tencent.news.core.platform

import com.tencent.news.core.audio.api.IFileCacheManager
import com.tencent.news.core.platform.api.IAppAlert
import com.tencent.news.core.platform.api.IAppCalendarReminder
import com.tencent.news.core.platform.api.IAppConfig
import com.tencent.news.core.platform.api.IAppDevice
import com.tencent.news.core.platform.api.IAppEncoder
import com.tencent.news.core.platform.api.IAppGyroscope
import com.tencent.news.core.platform.api.IAppInstallInfo
import com.tencent.news.core.platform.api.IAppLocation
import com.tencent.news.core.platform.api.IAppMediaPicker
import com.tencent.news.core.platform.api.IAppPageStack
import com.tencent.news.core.platform.api.IAppPermission
import com.tencent.news.core.platform.api.IAppRegex
import com.tencent.news.core.platform.api.IAppReport
import com.tencent.news.core.platform.api.IAppSecretConfig
import com.tencent.news.core.platform.api.IAppSkinManager
import com.tencent.news.core.platform.api.IAppStatus
import com.tencent.news.core.platform.api.IAppUri
import com.tencent.news.core.platform.api.IAppVibration
import com.tencent.news.core.platform.api.IEventBus
import com.tencent.news.core.platform.api.IFileManager
import com.tencent.news.core.platform.api.INetwork
import com.tencent.news.core.platform.api.IResManager
import com.tencent.news.core.platform.api.IScreenInfo
import com.tencent.news.core.platform.api.IStatusBarController
import com.tencent.news.core.platform.api.IStorage
import com.tencent.news.core.platform.api.ITask
import com.tencent.news.core.platform.api.IAppWindow


/**
 * 三端平台基础能力注册表。
 *
 * base-core 只定义能力契约和默认实现入口；具体 SDK 初始化、宿主上下文和业务参数由各端壳工程或业务 core 注入。
 */

interface IPlatformLogic {
    // 【app基础信息相关】
    val appDevice: IAppDevice?               // 特定平台app设备信息（折叠屏、判定华为/小米渠道等）
    val appStatus: IAppStatus?               // app基础状态：例如是否debug等等
    val appConfig: IAppConfig?               // 腾讯系远程配置/实验能力：Shiply、TabExp 等
    val appSecretConfig: IAppSecretConfig?   // 私密配置，例如：app key、app secret等等，固定但又不能公开的参数

    // 【通用基础工具能力】
    val appInstallInfo: IAppInstallInfo?     // 宿主检测是否安装特定APP
    val appStorage: IStorage?                // app存储
    val fileManager: IFileManager?           // 文件操作模块
    val resManager: IResManager?             // 资源管理（预加载图片等等）
    val skinManager: IAppSkinManager?        // 皮肤资源管理（频道皮肤、节日皮肤等）
    val appReport: IAppReport?               // 腾讯系基础上报/诊断能力：Beacon、Bugly、DT 等
    val appAlert: IAppAlert?                 // 弹窗通知、toast、气泡 等
    val appEncoder: IAppEncoder?             // 各类编解码
    val appUri: IAppUri?                     // Uri解析（安卓/iOS不注入也行，主要是给鸿蒙）
    val task: ITask?                         // 异步、延迟、主线程任务调度
    val eventBus: IEventBus?                 // 事件广播
    val network: INetwork?                   // 通用网络请求
    val appPageStack: IAppPageStack?         // 页面栈管理
    val fileCacheManager: IFileCacheManager? // 本地文件缓存管理：缓存查询，删除，增加
    val vibration: IAppVibration?            // 手机振动
    val gyroscope: IAppGyroscope?            // 陀螺仪传感器（支持 Android/iOS/鸿蒙）
    val appPermission: IAppPermission?       // 权限管理
    val appLocation: IAppLocation?           // 定位
    val appRegex: IAppRegex?                 // 正则匹配
    val systemVolumeController: ISystemVolumeController? // 系统音量控制
    val systemBrightnessController: ISystemBrightnessController? get() = null // 当前窗口亮度控制
    val calendarReminder: IAppCalendarReminder? get() = null                 // 系统日历提醒
    val statusBarController: IStatusBarController?       // 系统StatusBar样式/显隐控制
    val appWindow: IAppWindow?                           // 窗口管理（屏幕常亮、方向、全屏等）
    val screenInfo: IScreenInfo?                         // 屏幕尺寸、密度等显示环境信息
    val appMediaPicker: IAppMediaPicker?              // 图片选择/上传


}

// 如果不想让宿主注入，必须用expect默认实现的话，把这里的var改成val
object QnPlatformLogic : IPlatformLogic, IPlatformInject {
    // 【app基础信息相关】
    override var appDevice = defaultImpl?.appDevice
    override var appStatus = defaultImpl?.appStatus
    override var appConfig = defaultImpl?.appConfig
    override var appSecretConfig: IAppSecretConfig? = null   // 私密配置，例如：app key、app secret等等，固定但又不能公开的参数

    // 【通用基础工具能力】
    override var appInstallInfo = defaultImpl?.appInstallInfo
    override var appStorage = defaultImpl?.appStorage
    override var fileManager = defaultImpl?.fileManager
    override var resManager = defaultImpl?.resManager
    override var skinManager = defaultImpl?.skinManager
    override var appReport = defaultImpl?.appReport
    override var appAlert = defaultImpl?.appAlert
    override var appEncoder = defaultImpl?.appEncoder
    override var appUri = defaultImpl?.appUri
    override var task = defaultImpl?.task
    override var eventBus = defaultImpl?.eventBus
    override var network = defaultImpl?.network
    override var appPageStack = defaultImpl?.appPageStack
    override var fileCacheManager = defaultImpl?.fileCacheManager
    override var vibration = defaultImpl?.vibration
    override var gyroscope = defaultImpl?.gyroscope
    override var appPermission = defaultImpl?.appPermission
    override var appLocation = defaultImpl?.appLocation
    override var appRegex = defaultImpl?.appRegex
    override var systemVolumeController = defaultImpl?.systemVolumeController
    override var systemBrightnessController = defaultImpl?.systemBrightnessController
    override var calendarReminder = defaultImpl?.calendarReminder
    override var statusBarController = defaultImpl?.statusBarController
    override var appWindow = defaultImpl?.appWindow
    override var screenInfo = defaultImpl?.screenInfo
    override var appMediaPicker: IAppMediaPicker? = null     // 图片选择/上传

}

private val defaultImpl get() = getDefaultPlatformLogic()

// 通过expect方式提供默认实现，简化setup注册操作，尽量开箱即用
internal expect fun getDefaultPlatformLogic(): IPlatformLogic?
