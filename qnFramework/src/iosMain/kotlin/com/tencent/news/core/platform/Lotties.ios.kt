package com.tencent.news.core.platform

actual val Lotties: ILottieRes = object : LottieResImpl(), ILottieRes {

    override val loadingPage: String
        get() = "file://loading.json"

    override val rotatingPullToRefresh: String
        get() = "file://rotating_pull_to_refresh.json"

    override val aigcDiscoveryLoading: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250415151325/Production/qn_group_shanghua_38_loding.zip"

    /**
     * 早报底层页头部lottie
     */
    override val morningPostHeader: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/20221121115354/qn_group_qingtian.zip"

    /**
     * 晚报底层页头部lottie
     */
    override val eveningPostHeader: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/20221121115354/qn_group_qingtian.zip"

    /**
     * 早报底层页TitleBar阅读完成lottie
     */
    override val morningPostReadComplete: String
        get() {
            return if (isPad()) {
                "file://local_morningpost_yiyue_ipad.json" // iPad专用链接
            } else {
                "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250519121631/Production/qn_group_zaobao_yiyue.zip"
            }
        }

    /**
     * 午报底层页TitleBar阅读完成lottie
     */
    override val noonPostReadComplete: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250109174222/Production/qn_group_wubao_yiyue.zip"

    /**
     * 晚报底层页TitleBar阅读完成lottie
     */
    override val eveningPostReadComplete: String
        get() {
            return if (isPad()) {
                "file://local_eveningpost_yiyue_ipad.json" // iPad专用链接
            } else {
                "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250519124359/Production/qn_group_wanbao_yiyue.zip"
            }
        }

    /**
     * 早晚报底层页AudioBar播放按钮lottie，共有3个形态：
     * 1. normal: 非播放形态，展示耳机
     * 2. loading: 加载形态，展示...
     * 3. playing: 播放形态
     */
    override val morningPostPlayAudioLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20250305210643/Production/qn_group_zaobao_yin.zip"

    /**
     * 早晚报底层页TitleBar中的播放按钮lottie，共有3个形态：
     * 1. normal: 非播放形态，展示耳机
     * 2. loading: 加载形态，展示...
     * 3. playing: 播放形态
     */
    override val playAudioLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20250306124223/Production/qn_group_erji.zip"

    /**
     * 早报底层页TitleBar阅读进度lottie
     */
    override val morningPostReadProgress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250109174553/Production/qn_group_diceng_jindu.zip"

    /**
     * 晚报底层页TitleBar阅读进度lottie
     */
    override val eveningPostReadProgress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250109174621/Production/qn_group_wanbao_jindu.zip"

    /**
     * 定制早报之后刷新主列表的动画
     */
    override val postRefresh: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20220607111226/423e9761a149658548f763ae98e58afe.zip"

    /**
     * 直播中动画
     */
    override val livePlayingLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250328152957/Production/volume_newStyle.zip"

    // 撒花动画
    override val confettiCelebration: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250422112305/Production/07.lottie"

    // 左看右看
    override val lookLeftRight: String
        get() = "file://lookLeftRight.json"

    override val recordWave: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250509114243/Production/wave.zip"

    override val sponsorFire: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250609195258/Production/44date.lottie"

    override val aigcRecordWave: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250610140707/Production/yuyin..zip"

    override val footerLoading: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250611160516/Production/shanghua_38_loading.json"

    override val volumeWave: String
        get() = "file://volume_newStyle.json"

    override val volumeControl: String
        get() = "file://video_immerse_mute_btn.json"

    override val aigcStream: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250715113316/Production/xwm.lottie"

    override val aigcVoicePress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20251212102851465/Production/voice_vector.zip"

    override val aiPodcastInteractPress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260113200415715/Production/voicer.json.zip"

    override val aigcVoiceCancel: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250804120908791/Production/cancel.zip"

    override val aigcChatLoading: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260317113553132/Production/loading.zip"

    override val audioPlayerListSelect: String
        get() = "file://listen_playing_menu.json"

    override val adVideoCareLike: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/20240326172906/normal.zip"

    override val podcastAudioPlayingLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20240812113515/Production/audio_ip_loading.zip"

    override val audioRadioPlayingLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260703113756502/Production/shoutinzhong1.json.zip"

    override val livePlaying: String
        get() = ""

    override val audioRadioGuide: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260209110650500/Production/qn_group_guanxin_shanghua.zip"

    override val audioRadioCardLongPress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260529114222302/Production/b.json.zip"

}
