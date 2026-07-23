package com.tencent.kmm.demo.startup.sdk

import com.tencent.kmm.startup.std.PlatformTask
import com.tencent.kmm.startup.std.PlatformTaskProvider
import com.tencent.kmm.startup.std.tasks.BeaconInitResult
import com.tencent.kmm.startup.std.tasks.MidasInitResult
import com.tencent.kmm.startup.std.tasks.WeComShareInitResult
import com.tencent.kmm.startup.std.tasks.WeiboShareInitResult
import com.tencent.kmm.startup.std.tasks.BuglyInitResult
import com.tencent.kmm.startup.std.tasks.QQLoginInitResult
import com.tencent.kmm.startup.std.tasks.QimeiInitResult
import com.tencent.kmm.startup.std.tasks.ReshubInitResult
import com.tencent.kmm.startup.std.tasks.WXLoginInitResult
import com.tencent.kmm.startup.std.tasks.ToggleInitResult
import com.tencent.kmm.startup.std.tasks.TabExpInitResult
import com.tencent.kmm.startup.std.tasks.TuringInitResult
import com.tencent.kmm.startup.std.tasks.UploadSdkInitResult
import com.tencent.kmm.demo.startup.sdk.tasks.initBeacon
import com.tencent.kmm.demo.startup.sdk.tasks.initBugly
import com.tencent.kmm.startup.std.tasks.initKmkv
import com.tencent.kmm.startup.std.tasks.initLottie
import com.tencent.kmm.demo.startup.sdk.logger.initLogger
import com.tencent.kmm.demo.startup.sdk.kuikly.initKuiklyAdapter
import com.tencent.kmm.demo.startup.sdk.tasks.initQQLogin
import com.tencent.kmm.demo.startup.sdk.tasks.initQimei
import com.tencent.kmm.demo.startup.sdk.tasks.initTabExp
import com.tencent.kmm.demo.startup.sdk.tasks.initWXLogin
import com.tencent.kmm.demo.startup.sdk.tasks.initReshub
import com.tencent.kmm.demo.startup.sdk.tasks.initMidas
import com.tencent.kmm.demo.startup.sdk.tasks.initWeComShare
import com.tencent.kmm.demo.startup.sdk.tasks.initWeiboShare
import com.tencent.kmm.demo.startup.sdk.tasks.initToggle
import com.tencent.kmm.demo.startup.sdk.tasks.initTuring
import com.tencent.kmm.demo.startup.sdk.tasks.initUploadSdk
import com.tencent.kmm.demo.startup.sdk.tasks.initOaid

class AndroidPlatformTaskProvider : PlatformTaskProvider {

    override val loggerInitTask: PlatformTask<Unit> = ::initLogger

    override val kuiklyAdapterInitTask: PlatformTask<Unit> = ::initKuiklyAdapter

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

    override val toggleInitTask: PlatformTask<ToggleInitResult> = ::initToggle

    override val turingInitTask: PlatformTask<TuringInitResult> = ::initTuring

    override val uploadSdkInitTask: PlatformTask<UploadSdkInitResult> = ::initUploadSdk

    override val kmkvInitTask: PlatformTask<Unit> = ::initKmkv

    override val lottieInitTask: PlatformTask<Unit> = ::initLottie

    override val oaidInitTask: PlatformTask<String> = ::initOaid

}
