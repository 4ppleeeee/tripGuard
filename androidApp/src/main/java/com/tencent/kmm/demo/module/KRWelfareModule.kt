package com.tencent.kmm.demo.module

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kmm.demo.core.feeds.welfare.vm.WelfarePendantNotify
import com.tencent.kmm.demo.core.feeds.welfare.vm.WelfarePendantNotifyRegistry
import org.json.JSONObject

/**
 * WSKuiklyWelfareModule 的 Android Native 桥。
 *
 * 福利页内嵌 Kuikly 子页面领取时长任务奖励后，会通过该模块通知播放侧挂件刷新/隐藏领取态。
 */
class KRWelfareModule : KuiklyRenderBaseModule() {

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            METHOD_VIDEO_TASK_END_NOTIFY -> {
                videoTaskEndNotify(params)
                callback?.invoke(successResult())
                null
            }

            METHOD_ACTIVE_WELFARE_FEATURE,
            METHOD_WELFARE_LAND_PAGE_EXPOSURE,
            METHOD_SHOW_CHECKIN_TIPS_DIALOG -> {
                Log.i(TAG, "$method no-op params=$params")
                callback?.invoke(successResult())
                null
            }

            else -> {
                Log.w(TAG, "method not found: $method")
                callback?.invoke(
                    mapOf(
                        "code" to -1,
                        "msg" to "method not found: $method",
                        "data" to successData(false),
                    )
                )
                null
            }
        }
    }

    private fun videoTaskEndNotify(params: String?) {
        val json = parseJson(params)
        val notify = WelfarePendantNotify(
            sceneType = json.optInt(KEY_SCENE_TYPE, 0),
            action = json.optString(KEY_ACTION, WelfarePendantNotify.ACTION_TASK_END),
            hasTaskNoReceiveGold = json.optBoolean(KEY_HAS_TASK_NO_RECEIVE_GOLD, false),
            forceShow = json.optBoolean(KEY_FORCE_SHOW, false),
        )
        Log.i(TAG, "videoTaskEndNotify notify=$notify params=$params")
        mainHandler.post {
            WelfarePendantNotifyRegistry.notify(notify)
        }
    }

    private fun parseJson(params: String?): JSONObject {
        return runCatching {
            if (params.isNullOrBlank()) JSONObject() else JSONObject(params)
        }.getOrElse { JSONObject() }
    }

    private fun successResult(): Map<String, Any> {
        return mapOf(
            "code" to 0,
            "msg" to "",
            "data" to successData(true),
        )
    }

    private fun successData(isSuccess: Boolean): Map<String, Int> {
        return mapOf("isSuccess" to if (isSuccess) 1 else 0)
    }

    companion object {
        const val MODULE_NAME = "WSKuiklyWelfareModule"

        private const val TAG = "KRWelfareModule"
        private const val METHOD_ACTIVE_WELFARE_FEATURE = "activeWelfareFeature"
        private const val METHOD_WELFARE_LAND_PAGE_EXPOSURE = "welfareLandPageExposure"
        private const val METHOD_VIDEO_TASK_END_NOTIFY = "videoTaskEndNotify"
        private const val METHOD_SHOW_CHECKIN_TIPS_DIALOG = "showCheckInTipsDialog"
        private const val KEY_SCENE_TYPE = "scene_type"
        private const val KEY_ACTION = "action"
        private const val KEY_HAS_TASK_NO_RECEIVE_GOLD = "has_task_no_receive_gold"
        private const val KEY_FORCE_SHOW = "force_show"
        private val mainHandler = Handler(Looper.getMainLooper())
    }
}
