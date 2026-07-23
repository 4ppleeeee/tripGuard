package com.tencent.kmm.demo.setup

import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.tencent.news.core.annotation.KmmInternalApi
import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.getRealContext
import com.tencent.news.core.compose.platform.IComposePageArgs
import com.tencent.news.core.platform.QnFrameworkLogic
import com.tencent.news.core.platform.api.ComponentRequest
import com.tencent.news.core.platform.api.IAppRouterBase
import com.tencent.news.core.pop.IPopType
import com.tencent.kmm.demo.KRApplication
import com.tencent.kmm.demo.KuiklyRenderActivity
import com.tencent.kmm.demo.view.AndroidComposeFragmentDialog

@KmmInternalApi
fun setupAndroidAppRouter() {
    QnFrameworkLogic.appRouter = AndroidAppRouter()
}

private class AndroidAppRouter : IAppRouterBase {

    private val app get() = KRApplication.application

    override suspend fun to(context: IKmmContext?, request: ComponentRequest) {
        requireNotNull(context)
        app.startActivity(
            Intent(request.item?.flexDto?.url).apply {
                addCategory("android.intent.category.DEFAULT")
                addCategory("android.intent.category.BROWSABLE")
                action = Intent.ACTION_VIEW
                flags = FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    override suspend fun to(context: IKmmContext?, scheme: String) {
        requireNotNull(context)
        try {
            app.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(scheme)).apply {
                    flags = FLAG_ACTIVITY_NEW_TASK
                }
            )
        } catch (e: Exception) {
            Log.w("AndroidAppRouter", "无法处理 scheme: $scheme", e)
        }
    }

    override suspend fun goBack(context: IKmmContext?) {
        Log.d("AndroidAppRouter", "goBack")
        val realActivity = context?.getRealContext() as? FragmentActivity ?: return
        realActivity.finish()
    }

    override suspend fun moveTaskToBack(context: IKmmContext?) {
        Log.d("AndroidAppRouter", "moveTaskToBack")
        val realActivity = context?.getRealContext() as? Activity ?: return
        realActivity.moveTaskToBack(true)
    }

    override suspend fun replace(context: IKmmContext?, pushAnimation: Boolean, scheme: String) {
        goBack(context)
        to(context, scheme)
    }

    override suspend fun toComposePage(
        context: IKmmContext?,
        pageName: String,
        pageArgs: IComposePageArgs
    ) {
        val realContext = context?.getRealContext() ?: return
        KuiklyRenderActivity.start(realContext, pageName, pageArgs)
    }

    override suspend fun toComposeDialog(
        context: IKmmContext?,
        popType: IPopType,
        pageName: String,
        pageArgs: IComposePageArgs
    ) {
        val realActivity = context?.getRealContext() as? FragmentActivity ?: return
        AndroidComposeFragmentDialog.showDialog(realActivity, pageName, pageArgs)
    }

    override suspend fun quit(context: IKmmContext?) {
        (context?.getRealContext() as? Activity)?.finish()
    }

}
