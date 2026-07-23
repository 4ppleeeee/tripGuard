package com.tencent.news.core.compose

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import org.json.JSONObject

internal class KuiklyRouterAdapter : IKRRouterAdapter {

    companion object {
        private const val KEY_PAGE_NAME = "pageName"
        private const val KEY_PAGE_DATA = "pageData"
        private const val KEY_HOT_RELOAD_IP = "hotReloadIp"
        private const val KEY_USE_DEX_MODE = "useDexMode"
    }

    override fun closePage(context: Context) {
        (context as? Activity)?.finish()
    }

    override fun openPage(context: Context, pageName: String, pageData: JSONObject, hotReloadIp: String) {
        val starter = Intent(context, AndroidComposePageDelegate::class.java)
        starter.putExtra(KEY_PAGE_NAME, pageName)
        starter.putExtra(KEY_PAGE_DATA, pageData.toString())
        starter.putExtra(KEY_HOT_RELOAD_IP, hotReloadIp)
        starter.putExtra(KEY_USE_DEX_MODE, pageData.optInt(KEY_USE_DEX_MODE))
        context.startActivity(starter)
    }
}