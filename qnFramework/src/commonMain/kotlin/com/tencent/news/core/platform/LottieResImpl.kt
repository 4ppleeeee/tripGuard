package com.tencent.news.core.platform

import com.tencent.news.core.platform.api.getShiplyConfig

// 三端统一的lottie资源，放这里（新版lottie库支持了）：
abstract class LottieResImpl : ILottieRes {

    override val aigcStream: String by lazy {
        getLottieUrl(
            "lottie_aigc_stream",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250715113316/Production/xwm.lottie"
        )
    }

    override val aigcHistoryLoading: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260121115246197/Production/xiala.zip"


    override val aigcVoicePress: String by lazy {
        getLottieUrl(
            "lottie_aigc_voice_press",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20251212102851465/Production/voice_vector.zip"
        )
    }

    override val aigcVoiceCancel: String by lazy {
        getLottieUrl(
            "lottie_aigc_voice_cancel",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250804120908791/Production/cancel.zip"
        )
    }

    override val aiPodcastInteractPress: String by lazy {
        getLottieUrl(
            "lottie_ai_podcast_interact_press",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260113200415715/Production/voicer.json.zip"
        )
    }

    override val aigcChatLoading: String by lazy {
        getLottieUrl(
            "lottie_aigc_chat_loading",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260317113553132/Production/loading.zip"
        )
    }

    // 作者成就海报弹窗撒花动画
    override val creatorAchievementPosterConfetti: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20260602111910811/Production/Confetti.json"

    // 竖版视频金币广告
    override val adVideoGold: String by lazy {
        getLottieUrl(
            "lottie_ad_video_gold",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20250815124806725/Production/ad_task_coin.zip"
        )
    }
    // 竖版视频红包广告 - 立即领取 （未点击时）
    override val adVideoRedPacketNotClicked: String by lazy {
        getLottieUrl(
            "lottie_ad_video_red_packet_not_clicked",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260105150259355/Production/hongbao2025.zip"
        )
    }

    // 竖版视频红包广告 - 领取成功 （已点击时）
    override val adVideoRedPacketClicked: String by lazy {
        getLottieUrl(
            "lottie_ad_video_red_packet_clicked",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260105150305647/Production/hongbao.zip"
        )
    }

    override val adVideoAvatarAnimBg: String by lazy {
        getLottieUrl(
            "lottie_ad_video_avatar_anim_bg",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20251226170857909/Production/normal.lottie"
        )
    }

    /**
     * 618 小店券右上角利益点挂件 Lottie。
     * getLottieUrl 会同步触达鸿蒙侧 Shiply JS 桥，使用 PUBLICATION 避免默认 SYNCHRONIZED lazy 锁链阻塞 JS 线程。
     */
    override val adShop618CouponHotTag: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        getLottieUrl(
            "lottie_ad_shop_618_coupon_hot_tag",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20260602162831990/Production/adShop618CouponHotTag_demo_fixed.zip"
        )
    }

    /**
     * 音频播放器长按倍速提示动画
     */
    override val audioPlayerLongPressSpeed: String by lazy {
        getLottieUrl(
            "lottie_audio_player_long_press_speed",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20251205131425214/Production/aibeisu.json.zip"
        )
    }

    /**
     * aigc 订阅早报 音频播放lottie
     */
    override val aigcMorningPostPlayAudioLottie: String by lazy {
        getLottieUrl(
            "aigc_morning_post_play_audio",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20251231192512595/Production/aigc_play_yinpin_zaobao.json.zip"
        )
    }

    override val audioRadioGuide: String by lazy {
        getLottieUrl(
            "qn_group_guanxin_shanghua",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260209110650500/Production/qn_group_guanxin_shanghua.zip"
        )
    }

    override val audioRadioCardLongPress: String by lazy {
        getLottieUrl(
            "lottie_audio_radio_card_long_press",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260529114222302/Production/b.json.zip"
        )
    }

    override val likeLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/20240710175531/Production/24.lottie"

    override val doubleTapLike: String by lazy {
        getLottieUrl(
            "lottie_double_tap_like",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/20240710175531/Production/24.lottie"
        )
    }

    /**
     * 语音输入波形, 蓝色
     */
    override val voiceInputBlue: String by lazy {
        getLottieUrl(
            "lottie_voice_input_blue",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260515160100869/Production/voicebarblue.zip"
        )
    }

    /**
     * 语音输入波形, 红色
     */
    override val voiceInputRed: String by lazy {
        getLottieUrl(
            "lottie_voice_input_red",
            "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260515160100834/Production/voicebarred.zip"
        )
    }

    // 方便链接配错后，能快速替换错误链接
    protected fun getLottieUrl(configName: String, defaultUrl: String): String {
        return getShiplyConfig(configName, defaultUrl)
    }
}