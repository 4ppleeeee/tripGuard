package com.tencent.kmm.demo

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic
import com.tencent.news.core.platform.qnFileLog

/**
 * 外部 Scheme 拉起中转 Activity。
 * Scheme 路由已从底座移除，这里仅记录入口并关闭。
 */
class ExternalSchemeJumpActivity : FragmentActivity(), IKmmContext {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        QnPlatformLogic.appPageStack?.onPageCreated(null, this)
        handleSchemeIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSchemeIntent(intent)
    }

    private fun handleSchemeIntent(intent: Intent?) {
        val schemeUri = intent?.data?.toString()
        qnFileLog()?.logD("ExternalSchemeJump", "scheme ignored: $schemeUri")
        if (!isFinishing) {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        QnPlatformLogic.appPageStack?.onPageDestroyed(null, this)
    }
}
