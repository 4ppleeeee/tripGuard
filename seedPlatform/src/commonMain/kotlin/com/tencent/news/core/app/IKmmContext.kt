package com.tencent.news.core.app

import com.tencent.news.core.platform.api.appPageStack

expect interface IKmmContext

/**
 * Provides a [IKmmContext] that can be used by  applications.
 */
val LocalKmmContext get() = LocalKmmContextProvider.current

object LocalKmmContextProvider {

    inline val current: IKmmContext?
        get() = appPageStack().getTopValidPage()

}