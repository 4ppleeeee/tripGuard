package com.tencent.kmm.startup.std

import com.tencent.kmm.startup.StartupContext
import com.tencent.kmm.startup.std.config.BeaconInitConfig
import com.tencent.kmm.startup.std.config.BuglyInitConfig
import com.tencent.kmm.startup.std.config.QQLoginInitConfig
import com.tencent.kmm.startup.std.config.ReshubInitConfig
import com.tencent.kmm.startup.std.config.TabExpInitConfig
import com.tencent.kmm.startup.std.config.ToggleInitConfig
import com.tencent.kmm.startup.std.config.UploadSdkInitConfig
import com.tencent.kmm.startup.std.config.WXLoginInitConfig
import com.tencent.kmm.startup.std.config.WeComShareInitConfig
import com.tencent.kmm.startup.std.config.WeiboShareInitConfig
import com.tencent.kmm.startup.std.tasks.BeaconInitResult
import com.tencent.kmm.startup.std.tasks.BuglyInitResult
import com.tencent.kmm.startup.std.tasks.MidasInitResult
import com.tencent.kmm.startup.std.tasks.QQLoginInitResult
import com.tencent.kmm.startup.std.tasks.QimeiInitResult
import com.tencent.kmm.startup.std.tasks.ReshubInitResult
import com.tencent.kmm.startup.std.tasks.TabExpInitResult
import com.tencent.kmm.startup.std.tasks.ToggleInitResult
import com.tencent.kmm.startup.std.tasks.TuringInitResult
import com.tencent.kmm.startup.std.tasks.UploadSdkInitResult
import com.tencent.kmm.startup.std.tasks.WXLoginInitResult
import com.tencent.kmm.startup.std.tasks.WeComShareInitResult
import com.tencent.kmm.startup.std.tasks.WeiboShareInitResult

/**
 * iOS 侧标准启动任务默认实现。
 *
 * 这里不直接绑定具体三方 SDK；真实 SDK 初始化仍可由 iOS 壳实现 [PlatformTaskProvider] 覆盖。
 */
open class IOSPlatformTaskProvider : PlatformTaskProvider {

    override val loggerInitTask: PlatformTask<Unit> = ::noOp

    override val kuiklyAdapterInitTask: PlatformTask<Unit> = ::noOp

    override val qimeiInitTask: PlatformTask<QimeiInitResult> = { _, callback ->
        callback(QimeiInitResult(qimei = "", qimei36 = ""))
    }

    override val tabExpInitTask: PlatformTask<TabExpInitResult> = { context, callback ->
        callback(TabExpInitResult(appId = context.configOrNull<TabExpInitConfig>()?.appId.orEmpty()))
    }

    override val qqLoginInitTask: PlatformTask<QQLoginInitResult> = { context, callback ->
        callback(QQLoginInitResult(appId = context.configOrNull<QQLoginInitConfig>()?.appId.orEmpty()))
    }

    override val wxLoginInitTask: PlatformTask<WXLoginInitResult> = { context, callback ->
        callback(WXLoginInitResult(appId = context.configOrNull<WXLoginInitConfig>()?.appId.orEmpty()))
    }

    override val weiboShareInitTask: PlatformTask<WeiboShareInitResult> = { context, callback ->
        callback(WeiboShareInitResult(appKey = context.configOrNull<WeiboShareInitConfig>()?.appKey.orEmpty()))
    }

    override val weComShareInitTask: PlatformTask<WeComShareInitResult> = { context, callback ->
        callback(WeComShareInitResult(shareAppId = context.configOrNull<WeComShareInitConfig>()?.shareAppId.orEmpty()))
    }

    override val buglyInitTask: PlatformTask<BuglyInitResult> = { context, callback ->
        callback(BuglyInitResult(appId = context.configOrNull<BuglyInitConfig>()?.appId.orEmpty()))
    }

    override val beaconInitTask: PlatformTask<BeaconInitResult> = { context, callback ->
        callback(BeaconInitResult(appKey = context.configOrNull<BeaconInitConfig>()?.appKey.orEmpty()))
    }

    override val reshubInitTask: PlatformTask<ReshubInitResult> = { context, callback ->
        val config = context.configOrNull<ReshubInitConfig>()
        callback(
            ReshubInitResult(
                appId = config?.appId.orEmpty(),
                env = if (config?.useTestEnv == true) "test" else "release",
            )
        )
    }

    override val midasInitTask: PlatformTask<MidasInitResult> = { context, callback ->
        callback(MidasInitResult(initialized = false, platform = context.platform.name.lowercase()))
    }

    override val toggleInitTask: PlatformTask<ToggleInitResult> = { context, callback ->
        val config = context.configOrNull<ToggleInitConfig>()
        callback(
            ToggleInitResult(
                appId = config?.appId.orEmpty(),
                env = if (config?.useTestEnv == true) "test" else "release",
            )
        )
    }

    override val turingInitTask: PlatformTask<TuringInitResult> = { _, callback ->
        callback(TuringInitResult())
    }

    override val uploadSdkInitTask: PlatformTask<UploadSdkInitResult> = { context, callback ->
        val config = context.configOrNull<UploadSdkInitConfig>()
        callback(
            UploadSdkInitResult(
                bizAppId = config?.bizAppId ?: 0,
                bizDomain = config?.bizDomain.orEmpty(),
            )
        )
    }

    override val kmkvInitTask: PlatformTask<Unit> = ::noOp
}

private fun noOp(context: StartupContext, callback: OnReceiveStartupTaskResult<Unit>) {
    callback(Unit)
}
