package com.tencent.news.core.compose.view

import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.view.lifecycle.ComposeEvent

internal interface IComposeContainerView {

    // @ComposeViewKey
    fun onCreate(composeViewKey: String, pageArgs: IComposePageArgs)

    fun onDestroy()

    fun sendEvent(event: ComposeEvent, params: Map<String, Any> = emptyMap())

    fun onResume()

    fun onPause()

}