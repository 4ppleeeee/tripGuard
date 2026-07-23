package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.std.tasks.BeaconInitResult
import com.tencent.kmm.startup.std.tasks.MidasInitResult
import com.tencent.kmm.startup.std.tasks.WeComShareInitResult
import com.tencent.kmm.startup.std.tasks.WeiboShareInitResult
import com.tencent.kmm.startup.std.tasks.BuglyInitResult
import com.tencent.kmm.startup.std.tasks.QQLoginInitResult
import com.tencent.kmm.startup.std.tasks.QimeiInitResult
import com.tencent.kmm.startup.std.tasks.ReshubInitResult
import com.tencent.kmm.startup.std.tasks.ToggleInitResult
import com.tencent.kmm.startup.std.tasks.TabExpInitResult
import com.tencent.kmm.startup.std.tasks.TuringInitResult
import com.tencent.kmm.startup.std.tasks.UploadSdkInitResult
import com.tencent.kmm.startup.std.tasks.WXLoginInitResult

interface PlatformTaskProvider {

    val loggerInitTask: PlatformTask<Unit>

    /**
     * Kuikly 适配器初始化任务
     */
    val kuiklyAdapterInitTask: PlatformTask<Unit>

    /**
     * Qimei 初始化任务
     */
    val qimeiInitTask: PlatformTask<QimeiInitResult>

    /**
     * TAB/Roma AB 实验 SDK 初始化任务
     */
    val tabExpInitTask: PlatformTask<TabExpInitResult>

    /**
     * QQ 登录 SDK 初始化任务
     */
    val qqLoginInitTask: PlatformTask<QQLoginInitResult>

    /**
     * 微信登录 SDK 初始化任务
     */
    val wxLoginInitTask: PlatformTask<WXLoginInitResult>

    /**
     * 新浪微博分享 SDK 初始化任务
     */
    val weiboShareInitTask: PlatformTask<WeiboShareInitResult>

    /**
     * 企业微信分享 SDK 初始化任务
     */
    val weComShareInitTask: PlatformTask<WeComShareInitResult>

    /**
     * Bugly 初始化任务
     */
    val buglyInitTask: PlatformTask<BuglyInitResult>

    /**
     * Beacon 初始化任务
     */
    val beaconInitTask: PlatformTask<BeaconInitResult>

    /**
     * Reshub 初始化任务
     */
    val reshubInitTask: PlatformTask<ReshubInitResult>

    /**
     * Midas 初始化任务
     */
    val midasInitTask: PlatformTask<MidasInitResult>

    /**
     * Shiply/Toggle 初始化任务
     */
    val toggleInitTask: PlatformTask<ToggleInitResult>

    /**
     * 图灵盾初始化任务
     */
    val turingInitTask: PlatformTask<TuringInitResult>

    /**
     * VME 上传中台 SDK 初始化任务
     */
    val uploadSdkInitTask: PlatformTask<UploadSdkInitResult>

    /**
     * MMKV 初始化任务
     */
    val kmkvInitTask: PlatformTask<Unit>

    /**
     * Lottie 动画库初始化任务（仅 Android 需要实现）
     */
    val lottieInitTask: PlatformTask<Unit>
        get() = { _, callback -> callback(Unit) }

    /**
     * 设备 OAID 初始化任务（仅 Android 需要实现）
     * 通过 VendorManager（qmsp-oaid2 SDK）获取设备 OAID。
     */
    val oaidInitTask: PlatformTask<String>
        get() = { _, callback -> callback("") }
}
