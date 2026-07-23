package com.tencent.kmm.demo.module.player

import android.os.Looper
import com.tencent.news.core.platform.api.appStatus
import com.tencent.qqlive.tvkplayer.TVideoMgr
import com.tencent.qqlive.tvkplayer.vinfo.TVKUserInfo
import com.tencent.superplayer.api.SuperPlayerVideoInfo
import com.tencent.superplayer.manager.UrlChangeManager
import com.tencent.superplayer.manager.UrlChangeManagerImpl
import com.tencent.superplayer.player.SuperPlayerConstants
import com.tencent.superplayer.vinfo.VInfoGetter
import com.tencent.kmm.demo.KRApplication
import com.tencent.kmm.demo.core.platform.api.appLoginBiz
import com.tencent.kmm.demo.core.service.VideoAuthService
import com.tencent.kmm.demo.core.service.api.ISuperPlayerBridge
import com.tencent.kmm.demo.core.service.api.ISuperPlayerCallback
import com.tencent.kmm.demo.core.service.api.TvkDefinitionInfo
import com.tencent.kmm.demo.core.service.api.TvkVideoInfo
import com.tencent.kmm.demo.library.log.WsVideoLogger

private const val TAG = "AndroidSuperPlayerBridge"

/**
 * Super Player SDK 的 vsKey，用于腾讯视频防盗链鉴权。
 * 对齐旧 app TencentLongVideoManager 中的 VS_KEY。
 */
private const val VS_KEY: String =
    "BNfE0TJHobzgKwDKTvZJS7ODfPmYdtv74UafElVU9pOSzfusXrz3TLGa3mUEajde2Jl2O274hvTgmnYtmLnoNaS+44gXj8csFtjOjUZRu" +
            "ErD0nY2bSHlcbamRP4BiJSrRWE2QcF4Np/Y6lcAQ/58Yf7IiKhkh216SDe9Tx1CtmSQji5WEJQVK9h4emWGE36Un0rGPx3ivX" +
            "wqbQAHH3xfvoMRXIU2L3+Ys03gZ7Ros6KZiWWUAQ4oHcEDYinl7VMdox5erhQw+esR1XdALAfgqBf2WK1CChuORW/jP5fFVue" +
            "6PKBEjg6jNPTycmm2fZOEndaU5nQCMwA5QNWacUl/qi5VOwQRze9/tPJY1l7ksy5nz12iBtY24/jUA8HHij0jmUbCZ1uL97DM" +
            "Kjq7p/mIewM/4mSzVd5eNPS7YhLHPM9AlgDGKJ0Q1yfqOXW8u6JlWbSXJJdilhyMhOhL2Dxl1znOOWmmE5vuXNphKRRFH8hK+" +
            "GRdJkYbAvR5NPwYlF7s"

/** Super Player SDK 的 sdtFrom，播放来源标识 */
private const val SDT_FROM: String = "v5041"

/**
 * Android 端 Super Player 原生桥接实现
 *
 * 通过 TVideoMgr + VInfoGetter 完成 SDK 初始化和 vid 换链。
 */
class AndroidSuperPlayerBridge : ISuperPlayerBridge {

    private val context get() = KRApplication.application

    override fun initSdk(platformId: String, qimei: String): Boolean {
        val appVersion = appStatus().getVersionName()
        val platId = platformId.toIntOrNull() ?: 110303
        TVideoMgr.init(context, platId, SDT_FROM, appVersion, qimei, VS_KEY)

        val params = UrlChangeManagerImpl.InitParams()
        params.platId = platId
        params.sdtFrom = SDT_FROM
        params.vsKey = VS_KEY
        UrlChangeManager.getInstance().initSdk(context, params)

        WsVideoLogger.i(TAG, "initSdk 完成: platformId=$platId, qimei=$qimei")
        return true
    }

    override fun getVideoInfo(
        videoId: String,
        cid: String,
        definition: String,
        callback: ISuperPlayerCallback
    ) {
        val videoInfo = buildSuperPlayerVideoInfo(videoId, definition)

        val videoInfoClient = VInfoGetter(context, Looper.getMainLooper())
        videoInfoClient.setListener(object : VInfoGetter.VInfoGetterListener {
            override fun onGetVInfoSuccess(resultVideoInfo: SuperPlayerVideoInfo) {
                val tvkVideoInfo = convertSuperPlayerResult(videoId, resultVideoInfo)
                callback.onResult(tvkVideoInfo)
            }

            override fun onGetVInfoFailed(
                resultVideoInfo: SuperPlayerVideoInfo,
                errorType: Int,
                errorCode: Int,
                detail: String?
            ) {
                WsVideoLogger.e(
                    TAG,
                    "vinfo 失败: vid=$videoId, errorType=$errorType, errorCode=$errorCode, detail=$detail"
                )
                callback.onResult(null)
            }
        })

        videoInfoClient.doGetVInfo(videoInfo)
    }

    /**
     * 构建 SuperPlayerVideoInfo 请求参数
     */
    private fun buildSuperPlayerVideoInfo(
        videoId: String,
        definition: String
    ): SuperPlayerVideoInfo {
        val videoInfo = SuperPlayerVideoInfo(
            SuperPlayerVideoInfo.VIDEO_SOURCE_TVIDEO,
            SuperPlayerConstants.TVIDEO_TYPE_ONLINE_VOD,
            videoId
        )
        videoInfo.requestDefinition = definition
        videoInfo.isUseHevc = false

        val login = appLoginBiz()
        val loginUserInfo = login.getMainLoginUserInfo()
        if (loginUserInfo.isStrictLogin()) {
            videoInfo.userInfo = TVKUserInfo().apply {
                isVip = VideoAuthService.isVip()
                vipType = VideoAuthService.getVipLevel()
                uin = VideoAuthService.getVideoUserId()
                loginCookie = VideoAuthService.getCookie()
            }
        }

        return videoInfo
    }

    /**
     * 将 SuperPlayerVideoInfo 结果转换为 TvkVideoInfo
     */
    private fun convertSuperPlayerResult(
        videoId: String,
        resultVideoInfo: SuperPlayerVideoInfo
    ): TvkVideoInfo {
        val netVideoInfo = resultVideoInfo.videoInfo
        val playUrl = netVideoInfo?.playUrl.orEmpty()
        val durationMs = ((netVideoInfo?.duration ?: 0) * 1000)

        val definitionList = netVideoInfo?.definitionList?.mapNotNull { defInfo ->
            if (defInfo == null || defInfo.defn.isNullOrEmpty()) return@mapNotNull null
            TvkDefinitionInfo(
                defn = defInfo.defn.orEmpty(),
                defnName = defInfo.defnName.orEmpty(),
                defnRate = defInfo.defnRate,
                fileSizeByte = defInfo.fileSize,
                fps = 0,
                isVipOnly = defInfo.isVip == 1
            )
        }.orEmpty()

        val currentDef = netVideoInfo?.cgiVideoInfo?.selectedFormat.orEmpty()

        return TvkVideoInfo(
            vid = netVideoInfo?.vid.orEmpty().ifEmpty { videoId },
            playUrl = playUrl,
            videoWidth = 0,
            videoHeight = 0,
            duration = durationMs,
            currentDefinition = currentDef,
            definitionList = definitionList,
            previewStartTimeMs = netVideoInfo?.prePlayStartPos ?: 0,
            previewDurationMs = netVideoInfo?.prePlayTime ?: 0,
            chargeState = 0,
            vst = netVideoInfo.st
        )
    }
}
