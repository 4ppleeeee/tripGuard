package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.std.PlatformTaskProvider
import com.tencent.kmm.startup.std.tasks.BuglyInitResult
import com.tencent.kmm.startup.std.tasks.BeaconInitResult
import com.tencent.kmm.startup.std.tasks.MidasInitResult
import com.tencent.kmm.startup.std.tasks.WeComShareInitResult
import com.tencent.kmm.startup.std.tasks.WeiboShareInitResult
import com.tencent.kmm.startup.std.tasks.QQLoginInitResult
import com.tencent.kmm.startup.std.tasks.QimeiInitResult
import com.tencent.kmm.startup.std.tasks.WXLoginInitResult
import com.tencent.kmm.startup.std.tasks.ReshubInitResult
import com.tencent.kmm.startup.std.tasks.ToggleInitResult
import com.tencent.kmm.startup.std.tasks.TabExpInitResult
import com.tencent.kmm.startup.std.tasks.TuringInitResult
import com.tencent.kmm.startup.std.tasks.UploadSdkInitResult
import com.tencent.kmm.startup.std.tasks.initBeacon
import com.tencent.kmm.startup.std.tasks.initBugly
import com.tencent.kmm.startup.std.tasks.initKmkv
import com.tencent.kmm.startup.std.tasks.initKuikly
import com.tencent.kmm.startup.std.tasks.initLogger
import com.tencent.kmm.startup.std.tasks.initWeComShare
import com.tencent.kmm.startup.std.tasks.initWeiboShare
import com.tencent.kmm.startup.std.tasks.initQQLogin
import com.tencent.kmm.startup.std.tasks.initQimei
import com.tencent.kmm.startup.std.tasks.initTabExp
import com.tencent.kmm.startup.std.tasks.initWXLogin
import com.tencent.kmm.startup.std.tasks.initReshub
import com.tencent.kmm.startup.std.tasks.initMidas
import com.tencent.kmm.startup.std.tasks.initToggle
import com.tencent.kmm.startup.std.tasks.initTuring
import com.tencent.kmm.startup.std.tasks.initUploadSdk

open class HarmonyPlatformTaskProvider : PlatformTaskProvider {

    override val loggerInitTask: PlatformTask<Unit> = ::initLogger

    override val kuiklyAdapterInitTask: PlatformTask<Unit> = ::initKuikly

    override val qimeiInitTask: PlatformTask<QimeiInitResult> = ::initQimei

    override val tabExpInitTask: PlatformTask<TabExpInitResult> = ::initTabExp

    override val qqLoginInitTask: PlatformTask<QQLoginInitResult> = ::initQQLogin

    override val wxLoginInitTask: PlatformTask<WXLoginInitResult> = ::initWXLogin

    override val weiboShareInitTask: PlatformTask<WeiboShareInitResult> = ::initWeiboShare

    override val weComShareInitTask: PlatformTask<WeComShareInitResult> = ::initWeComShare

    override val buglyInitTask: PlatformTask<BuglyInitResult> = ::initBugly

    override val beaconInitTask: PlatformTask<BeaconInitResult> = ::initBeacon

    override val reshubInitTask: PlatformTask<ReshubInitResult> = ::initReshub

    override val midasInitTask: PlatformTask<MidasInitResult> = ::initMidas

    override val kmkvInitTask: PlatformTask<Unit> = ::initKmkv

    override val toggleInitTask: PlatformTask<ToggleInitResult> = ::initToggle

    override val turingInitTask: PlatformTask<TuringInitResult> = ::initTuring

    override val uploadSdkInitTask: PlatformTask<UploadSdkInitResult> = ::initUploadSdk

    // Lottie 动画库鸿蒙暂不支持，使用接口默认 no-op 实现
    // oaidInitTask 鸿蒙无设备 OAID 概念，使用接口默认 no-op 实现

}
