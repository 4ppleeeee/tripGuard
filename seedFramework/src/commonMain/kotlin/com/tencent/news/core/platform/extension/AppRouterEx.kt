package com.tencent.news.core.platform.extension

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.compose.platform.StructCellArgs
import com.tencent.news.core.compose.platform.emptyPageArgs
import com.tencent.news.core.list.model.IListItem
import com.tencent.news.core.platform.api.appRouter
import com.tencent.news.core.pop.IPopType
import com.tencent.news.core.router.contants.FrameworkViewKey
import com.tencent.news.core.util.CoroutineEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object AppRouterEx {

    fun toComposePage(
        context: IKmmContext? = LocalKmmContext,
        pageName: String,
        pageArgs: IComposePageArgs
    ) {
        CoroutineEx.syncRun {
            appRouter().toComposePage(context, pageName, pageArgs)
        }
    }

    fun toComposeDialog(
        context: IKmmContext? = LocalKmmContext,
        popType: IPopType,
        pageName: String,
        pageArgs: IComposePageArgs
    ) {
        CoroutineEx.syncRun {
            appRouter().toComposeDialog(context, popType, pageName, pageArgs)
        }
    }

    fun goBack() {
        CoroutineEx.syncRun {
            appRouter().goBack(LocalKmmContext)
        }
    }

    // 业务体验Demo
    fun toAppDemoPage() {
        toComposePage(pageName = FrameworkViewKey.Debug.DEMO_PAGE, pageArgs = emptyPageArgs())
    }

    // 开发者选项
    fun toAppDebugPage() {
        toComposePage(pageName = FrameworkViewKey.Setting.DEVELOPER, pageArgs = emptyPageArgs())
    }

    fun toScheme(scheme: String) = toScheme(LocalKmmContext, scheme)

    fun toScheme(context: IKmmContext?, scheme: String) {
        CoroutineScope(Dispatchers.Main).launch {
            appRouter().to(context, scheme)
        }
    }

    fun IListItem.debugPreviewComposeCell() {
        toComposePage(
            pageName = FrameworkViewKey.Channel.ITEM_CELL,
            pageArgs = StructCellArgs.simpleCreate(this)
        )
    }

}
