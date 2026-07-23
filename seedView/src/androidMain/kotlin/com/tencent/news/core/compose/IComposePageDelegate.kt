package com.tencent.news.core.compose

import android.app.Application
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.vm.IComposeContext

interface IComposePageDelegate : IComposeContext {

    var pageArgs: IComposePageArgs?
    var pageName: String?

    fun onCreate(
        app: Application,
        rootView: ViewGroup,
        pageArgs: IComposePageArgs,
        pageName: String,
        modules: Map<String, KuiklyRenderBaseModule> = emptyMap(),
        onFirstFrame: (() -> Unit)? = null,
        size: Size? = null
    )

    fun onNewIntent(args: IComposePageArgs)

    fun onResume()

    fun onPause()

    fun onDestroy()

    fun onDispatchBackEvent(): Boolean

    fun onDispatchTouchEvent(event: MotionEvent): Boolean

    fun sendEvent(event: String, data: Map<String, Any>)

    override fun findNativeView(nativeViewRef: Int): View?

}