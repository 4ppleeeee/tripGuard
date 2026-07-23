package com.tencent.news.core.platform

actual val Lotties: ILottieRes = object : LottieResImpl(), ILottieRes {

    override val loadingPage: String
        get() = "animation/qn_group_loading.lottie"

    override val rotatingPullToRefresh: String
        get() = "animation/rotating_pull_to_refresh.lottie"

    override val aigcDiscoveryLoading: String
        get() = "animation/qn_group_shanghua_38_loding.lottie"

    override val aigcStream: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250715113316/Production/xwm.lottie"

    override val aigcVoicePress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20251212102851465/Production/voice_vector.zip"

    override val aigcVoiceCancel: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250804120908791/Production/cancel.zip"

    /**
     * AI播客交互lottie
     */
    override val aiPodcastInteractPress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260113200415715/Production/voicer.json.zip"

    /**
     * 早报底层页头部lottie
     */
    override val morningPostHeader: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20211121182820/5251454a370607417a71499d1ca9c139.lottie"

    /**
     * 晚报底层页头部lottie
     */
    override val eveningPostHeader: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20220121190907/fb32a6aa112c9eb7b666e480e03213df.lottie"

    /**
     * 早报底层页TitleBar阅读完成lottie
     */
    override val morningPostReadComplete: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20220123144040/fd56f7723987923312c69f139c42acb9.lottie"

    /**
     * 晚报底层页TitleBar阅读完成lottie
     */
    override val eveningPostReadComplete: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20220123143948/2dba1e765535932547eb179de278edbd.lottie"


    /**
     * 早报底层页TitleBar阅读进度lottie
     */
    override val morningPostReadProgress: String
        get() = "https://s.inews.gtimg.com/inewsapp/QQNews_android/lottie/tag/qn_group_jindu_9c4a2f6ea24557bed5d98432ce4c85db.lottie"

    /**
     * 午报底层页TitleBar阅读完成lottie
     */
    override val noonPostReadComplete: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20220123143922/65da17a05db71c24d65195ccbf65ccda.lottie"


    /**
     * 晚报底层页TitleBar阅读进度lottie
     */
    override val eveningPostReadProgress: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20220121190559/8763de6b9374d638e34e75d66abdb0cf.lottie"

    /**
     * 早晚报底层页AudioBar播放按钮lottie，共有3个形态：
     * 1. normal: 非播放形态，展示耳机
     * 2. loading: 加载形态，展示...
     * 3. playing: 播放形态
     */
    override val morningPostPlayAudioLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20241009172630/Production/qn_group_zaobao_yinpin.lottie"

    /**
     * 早晚报底层页TitleBar中的播放按钮lottie，共有3个形态：
     * 1. normal: 非播放形态，展示耳机
     * 2. loading: 加载形态，展示...
     * 3. playing: 播放形态
     */
    override val playAudioLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/20230512150824/qn_group_erji.lottie"

    /**
     * 定制早报之后刷新主列表的动画
     */
    override val postRefresh: String
        get() = "https://s.inews.gtimg.com/tencentnews/_dasheng_/20220607110858/423e9761a149658548f763ae98e58afe.lottie"

    /**
     * 直播中动画
     */
    override val livePlayingLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250328152928/Production/volume_newStyle.lottie"

    // 撒花动画
    override val confettiCelebration: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250422112305/Production/07.lottie"

    // 作者成就海报弹窗撒花动画
    override val creatorAchievementPosterConfetti: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_ios/commonfile/formal/20260604173838382/Production/data.json.zip"

    // 左看右看
    override val lookLeftRight: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250423112725/Production/data.lottie"

    override val recordWave: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250509114754/Production/wave.lottie"

    override val sponsorFire: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250609195258/Production/44date.lottie"

    override val aigcRecordWave: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250610140707/Production/yuyin..zip"

    override val footerLoading: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20250611154710/Production/qn_group_shanghua_38_loding.lottie"

    override val volumeWave: String
        get() = "animation/icon_video_playing.lottie"

    override val volumeControl: String
        get() = "animation/mute_icon.json"

    override val audioPlayerListSelect: String
        get() = "animation/tingting_play_tips.json"

    override val adVideoCareLike: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/20240328160612/normal.lottie"

    override val podcastAudioPlayingLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20240812113515/Production/audio_ip_loading.zip"

    override val audioRadioPlayingLottie: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260703113756502/Production/shoutinzhong1.json.zip"

    override val livePlaying: String
        get() = ""

    override val audioRadioGuide: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260111204912540/Production/qn_group_guanxin_shanghua.lottie"

    override val audioRadioCardLongPress: String
        get() = "https://news-bz-1258344701.shiply-cdn.qq.com/reshub/qqnews_android/commonfile/formal/20260529114222302/Production/b.json.zip"

}
