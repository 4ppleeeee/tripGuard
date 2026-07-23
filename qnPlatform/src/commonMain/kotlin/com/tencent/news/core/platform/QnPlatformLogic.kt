@file:Suppress("unused")

package com.tencent.news.core.platform

import com.tencent.news.core.audio.api.IFileCacheManager
import com.tencent.news.core.platform.api.AppI18nHolder
import com.tencent.news.core.platform.api.IAppAlert
import com.tencent.news.core.platform.api.IAppCalendarReminder
import com.tencent.news.core.platform.api.IAppConfig
import com.tencent.news.core.platform.api.IAppDevice
import com.tencent.news.core.platform.api.IAppEncoder
import com.tencent.news.core.platform.api.IAppGyroscope
import com.tencent.news.core.platform.api.IAppI18n
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
import com.tencent.news.core.platform.api.IStatusBarController
import com.tencent.news.core.platform.api.IStorage
import com.tencent.news.core.platform.api.ITask


/**
 * 由双端业务侧注入的业务逻辑
 */

object QnPlatformLogic : IPlatformInject {

    // 【app基础信息相关】
    var appDevice: IAppDevice? = null               // 特定平台app设备信息（折叠屏、判定华为/小米渠道等）
    var appStatus: IAppStatus? = null               // app基础状态：例如是否debug等等
    var appConfig: IAppConfig? = null               // 获取配置：shiply、tab等等
    var appSecretConfig: IAppSecretConfig? = null   // 私密配置，例如：app key、app secret等等，固定但又不能公开的参数

    // 【通用基础工具能力】
    var appInstallInfo: IAppInstallInfo? = null     // 宿主检测是否安装特定APP
    var appStorage: IStorage? = null                // app存储
    var fileManager: IFileManager? = null           // 文件操作模块
    var resManager: IResManager? = null             // 资源管理（预加载图片等等）
    var skinManager: IAppSkinManager? = null        // 皮肤资源管理（频道皮肤、节日皮肤等）
    var appReport: IAppReport? = null               // 上报相关（灯塔、bugly、大同）
    var appAlert: IAppAlert? = null                 // 弹窗通知、toast、气泡 等
    var appEncoder: IAppEncoder? = null             // 各类编解码
    var appUri: IAppUri? = null                     // Uri解析（安卓/iOS不注入也行，主要是给鸿蒙）
    var task: ITask? = null                         // 异步、延迟、主线程任务调度
    var eventBus: IEventBus? = null                 // 事件广播
    var network: INetwork? = null                   // 通用网络请求
    var appPageStack: IAppPageStack? = null         // 页面栈管理
    var fileCacheManager: IFileCacheManager? = null // 本地文件缓存管理：缓存查询，删除，增加
    var vibration: IAppVibration? = null            // 手机振动
    var gyroscope: IAppGyroscope? = null            // 陀螺仪传感器（支持 Android/iOS/鸿蒙）
    var appPermission: IAppPermission? = null       // 权限管理
    var appLocation: IAppLocation? = null           // 定位
    var appMediaPicker: IAppMediaPicker? = null     // 图片选择/上传
    var appRegex: IAppRegex? = null                 // 正则匹配
    var calendarReminder: IAppCalendarReminder? = null // 系统日历提醒
    var systemVolumeController: ISystemVolumeController? = null // 系统音量控制
    var systemBrightnessController: ISystemBrightnessController? = null // 系统（窗口）亮度控制
    var statusBarController: IStatusBarController? = null       // 系统StatusBar控制

    // 【多语言】
    var i18n: IAppI18n? = null                 // 多语言解析与语言切换订阅
        set(value) {
            field = value
            value?.let { AppI18nHolder.set(it) }
        }
}
