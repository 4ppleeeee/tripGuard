package com.tencent.kmm.demo.home

import com.tencent.news.core.platform.QnPlatformLogic

data class DemoEntry(
    val id: String,
    val title: String,
    val desc: String,
    val pageName: String,
)

data class PlatformCapabilityItem(
    val key: String,
    val title: String,
    val injected: Boolean,
    val desc: String,
)

fun buildDemoEntries(): List<DemoEntry> = listOf(
    DemoEntry(
        id = "seed_main_tab",
        title = "qnFramework 品字形页面",
        desc = "进入 SeedMainTabPage，验证 qnFramework/qnView 页面骨架",
        pageName = DemoRoutes.MAIN_TAB,
    ),
    DemoEntry(
        id = "platform_capabilities",
        title = "qnPlatform 能力注入",
        desc = "查看平台能力注入状态并执行交互测试",
        pageName = DemoRoutes.PLATFORM_CAPABILITIES,
    ),
)

fun buildPlatformCapabilityItems(): List<PlatformCapabilityItem> {
    buildPlatformCapabilityTestGroups()
    return listOf(
        capability("appDevice", "设备信息", QnPlatformLogic.appDevice != null),
        capability("appStatus", "应用状态", QnPlatformLogic.appStatus != null),
        capability("appConfig", "配置开关", QnPlatformLogic.appConfig != null),
        capability("appInstallInfo", "安装检测", QnPlatformLogic.appInstallInfo != null),
        capability("appStorage", "本地存储", QnPlatformLogic.appStorage != null),
        capability("fileManager", "文件管理", QnPlatformLogic.fileManager != null),
        capability("resManager", "资源管理", QnPlatformLogic.resManager != null),
        capability("appReport", "基础上报", QnPlatformLogic.appReport != null),
        capability("appAlert", "弹窗提示", QnPlatformLogic.appAlert != null),
        capability("appEncoder", "编解码", QnPlatformLogic.appEncoder != null),
        capability("appUri", "Uri 解析", QnPlatformLogic.appUri != null),
        capability("task", "任务调度", QnPlatformLogic.task != null),
        capability("eventBus", "事件总线", QnPlatformLogic.eventBus != null),
        capability("network", "网络请求", QnPlatformLogic.network != null),
        capability("appPageStack", "页面栈", QnPlatformLogic.appPageStack != null),
        capability("fileCacheManager", "文件缓存", QnPlatformLogic.fileCacheManager != null),
        capability("vibration", "震动", QnPlatformLogic.vibration != null),
        capability("gyroscope", "陀螺仪", QnPlatformLogic.gyroscope != null),
        capability("appPermission", "权限", QnPlatformLogic.appPermission != null),
        capability("appLocation", "定位", QnPlatformLogic.appLocation != null),
        capability("appRegex", "正则", QnPlatformLogic.appRegex != null),
        capability("systemVolumeController", "系统音量", QnPlatformLogic.systemVolumeController != null),
        capability("statusBarController", "状态栏", QnPlatformLogic.statusBarController != null),
        capability("appWindow", "窗口控制", QnPlatformLogic.appWindow != null),
        capability("screenInfo", "屏幕信息", QnPlatformLogic.screenInfo != null),
    )
}

private fun capability(key: String, title: String, injected: Boolean): PlatformCapabilityItem =
    PlatformCapabilityItem(
        key = key,
        title = title,
        injected = injected,
        desc = if (injected) "宿主已注入实现" else "未注入，使用默认实现或暂不可测",
    )
