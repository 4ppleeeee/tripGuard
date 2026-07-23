package com.tencent.news.core.service

import com.tencent.news.core.page.model.IShareBtnWidgetViewModel
import com.tencent.news.core.page.model.ShareBtnWidget

/**
 * qnFramework 分享能力桥接器。
 *
 * 默认实现不创建分享 VM；业务 core 需要分享入口时再注册真实实现。
 */
object FrameworkShareBridge {

    var impl: IFrameworkShareBridge = EmptyFrameworkShareBridge
        private set

    fun register(bridge: IFrameworkShareBridge) {
        impl = bridge
    }

}

interface IFrameworkShareBridge {

    fun createShareBtnVM(widget: ShareBtnWidget): IShareBtnWidgetViewModel

}

private object EmptyFrameworkShareBridge : IFrameworkShareBridge {
    override fun createShareBtnVM(widget: ShareBtnWidget): IShareBtnWidgetViewModel {
        error("Share button view model is not provided by kmm framework.")
    }
}
