package com.tencent.news.core.platform.api

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.platform.QnPlatformLogic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun appLocation(): IAppLocation = AppLocationInterceptor(QnPlatformLogic.appLocation)

interface IAppLocation {

    /**
     * 请求定位权限
     * @param context 页面
     * @param scenes 什么场景
     * @param callback 权限回调
     * @param requestLocationOnGrand 拿到权限后是否马上定位
     * @param isForceRequestPermission 强制给被动授权场景弹授权窗。该值为false时，新闻自己做的
     * 权限弹窗次数耗尽，直接返回定位失败，不会给用户感知。该值为true，则在新闻自己做的权限弹窗次数耗尽后，直接弹系统
     * 授权窗，系统授权窗之前被用户设置为一直不允许，则出toast引导
     *
     */
    suspend fun requestLocation(
        context: IKmmContext?,
        @LocationPermissionScenes scenes: Int,
        isForceRequestPermission: Boolean = false,
        requestLocationOnGrand: Boolean = false,
        callback: ILocationCallBack?,
    )

    /**
     * 静默获取adcode。
     *
     * - 有定位权限：缓存优先直接返回，否则发起定位并返回空。
     * - 无定位权限直接返回空
     */
    fun silentGetAdCode(@LocationPermissionScenes scenes: Int = LocationPermissionScenes.RECOMMEND): String? =
        null

    /**
     * Android使用，当前页面需要定位的话，在定位之前调用enterModule，在不需要定位后调用leaveModule
     */
    fun enterModule(@LocationPermissionScenes scenes: Int) {

    }

    /**
     * Android使用，当前页面需要定位的话，在定位之前调用enterModule，在不需要定位后调用leaveModule
     */
    fun leaveModule(@LocationPermissionScenes scenes: Int) {

    }
}

/**
 * 允许请求位置权限的场景
 */
annotation class LocationPermissionScenes {
    companion object {
        const val RECOMMEND = 1            // 推荐
        const val PUB_COMMENT = 2          // 发表评论
        const val PUB_WEIBO = 3            // 发布动态
        const val CHANGE_PROFILE_LOCAL = 4 // 个人资料编辑界面
        const val POST_WEATHER = 5         // 早晚报头部天气模块
        const val AIGC_POST_DETAIL = 6     // aigc早晚报底层弹窗
        const val AIGC_AGENT_DETAIL = 7    // aigc智能体底层
    }
}

fun interface ILocationCallBack {
    fun onLocationRequested(adCode: String)
}

private class AppLocationInterceptor(private val platformLocation: IAppLocation?) : IAppLocation {
    override suspend fun requestLocation(
        context: IKmmContext?,
        scenes: Int,
        isForceRequestPermission: Boolean,
        requestLocationOnGrand: Boolean,
        callback: ILocationCallBack?,
    ) {
        withContext(Dispatchers.Main) {
            platformLocation?.requestLocation(
                context,
                scenes,
                isForceRequestPermission,
                requestLocationOnGrand,
                callback
            )
        }
    }

    override fun silentGetAdCode(scenes: Int): String {
        return platformLocation?.silentGetAdCode(scenes) ?: ""
    }
}

