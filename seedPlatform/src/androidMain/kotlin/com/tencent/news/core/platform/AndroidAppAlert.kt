package com.tencent.news.core.platform

import android.content.Context
import android.widget.Toast
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.platform.api.IAppAlert
import com.tencent.news.core.platform.api.appTask

/**
 * Android 平台的 IAppAlert 默认实现
 * 参考 QnCore 工程的 AndroidAppAlert 实现
 */
class AndroidAppAlert : IAppAlert {

    override fun checkShowPushRemindDialog(type: String) {
    }

    override fun showToast(msg: String, duration: Double, debug: Boolean) {
        appTask().runMainAction {
            val context = LocalKmmContext as? Context ?: return@runMainAction
            val toastDuration = if (duration > IAppAlert.ALERT_SHORT_TIME) {
                Toast.LENGTH_LONG
            } else {
                Toast.LENGTH_SHORT
            }
            Toast.makeText(context, msg, toastDuration).show()
        }
    }

    override fun showDialog(title: String, msg: String) {
        showToast(title + "\n" + msg)
    }

    override fun showDecorDebugView(msg: String) {
        // 默认不处理
    }

    override fun hideCurrentToast() {
        // 默认不处理
    }
}
