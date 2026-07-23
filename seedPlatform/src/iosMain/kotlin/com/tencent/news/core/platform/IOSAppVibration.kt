package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.IAppVibration
import platform.Foundation.NSThread
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS 平台默认震动实现。
 */
internal object IOSAppVibration : IAppVibration {
    override fun triggerVibration() {
        onMain {
            val generator = UIImpactFeedbackGenerator(
                style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
            )
            generator.prepare()
            generator.impactOccurred()
        }
    }

    private inline fun onMain(noinline block: () -> Unit) {
        if (NSThread.isMainThread) {
            block()
        } else {
            dispatch_async(dispatch_get_main_queue(), block)
        }
    }
}

fun setupIOSAppVibration(vibration: IAppVibration) {
    QnPlatformLogic.vibration = vibration
}
