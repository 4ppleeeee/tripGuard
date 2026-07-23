package com.tencent.kmm.demo.startup.sdk.kuikly

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import org.json.JSONObject

object KRRouterAdapter : IKRRouterAdapter {

    override fun openPage(
        context: Context,
        pageName: String,
        pageData: JSONObject,
        hotReloadIp: String,
    ) {
        val starter = Intent().apply {
            setClassName(context, KUIKLY_RENDER_ACTIVITY_CLASS_NAME)
            putExtra(KEY_PAGE_NAME, pageName)
            putExtra(KEY_PAGE_DATA, pageData.toString())
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(starter)
    }

    override fun closePage(context: Context) {
        (context as? Activity)?.finish()
    }

    private const val KUIKLY_RENDER_ACTIVITY_CLASS_NAME = "com.tencent.kmm.demo.KuiklyRenderActivity"
    private const val KEY_PAGE_NAME = "pageName"
    private const val KEY_PAGE_DATA = "pageData"
}
