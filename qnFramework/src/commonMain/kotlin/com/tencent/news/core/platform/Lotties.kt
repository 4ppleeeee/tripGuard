package com.tencent.news.core.platform

expect val Lotties: ILottieRes

interface ILottieRes {

    val loadingPage: String

    val rotatingPullToRefresh: String

    val aigcHistoryLoading: String

    val aigcDiscoveryLoading: String

    val aigcStream: String

    val aigcVoicePress: String

    val aigcVoiceCancel: String

    /**
     * AI播客交互lottie
     */
    val aiPodcastInteractPress: String

    /**
     * 早报底层页头部lottie
     */
    val morningPostHeader: String

    /**
     * 晚报底层页头部lottie
     */
    val eveningPostHeader: String

    /**
     * 早报底层页TitleBar阅读完成lottie
     */
    val morningPostReadComplete: String

    /**
     * 晚报底层页TitleBar阅读完成lottie
     */
    val eveningPostReadComplete: String

    /**
     * 早晚报底层页AudioBar播放按钮lottie，共有3个形态：
     * 1. normal: 非播放形态，展示耳机
     * 2. loading: 加载形态，展示...
     * 3. playing: 播放形态
     */
    val morningPostPlayAudioLottie: String

    /**
     * 早晚报底层页TitleBar中的播放按钮lottie，共有3个形态：
     * 1. normal: 非播放形态，展示耳机
     * 2. loading: 加载形态，展示...
     * 3. playing: 播放形态
     */
    val playAudioLottie: String

    /**
     * 早报底层页TitleBar阅读进度lottie
     */
    val morningPostReadProgress: String

    /**
     * 午报底层页TitleBar阅读完成lottie
     */
    val noonPostReadComplete: String


    /**
     * 晚报底层页TitleBar阅读进度lottie
     */
    val eveningPostReadProgress: String

    /**
     * 定制早报之后刷新主列表的动画
     */
    val postRefresh: String

    /**
     * 直播中动画
     */
    val livePlayingLottie: String

    // 撒花动画
    val confettiCelebration: String

    // 作者成就海报弹窗撒花动画
    val creatorAchievementPosterConfetti: String

    // 左看右看
    val lookLeftRight: String

    val recordWave: String

    val sponsorFire: String

    val aigcRecordWave: String

    // 上滑刷新lottie
    val footerLoading: String

    // 音量波纹
    val volumeWave: String

    // 音量控制
    val volumeControl: String

    // 新闻搜索资料动画
    val aigcChatLoading: String

    // 竖版视频金币广告
    val adVideoGold: String

    // 竖版视频红包广告 - 立即领取（未点击时）
    val adVideoRedPacketNotClicked: String

    // 竖版视频红包广告 - 领取成功（已点击时）
    val adVideoRedPacketClicked: String

    val audioPlayerListSelect: String

    val adVideoCareLike: String

    val adVideoAvatarAnimBg: String

    /**
     * 618 小店券右上角利益点挂件 Lottie。
     */
    val adShop618CouponHotTag: String

    // 播客页面白色播放动效
    val podcastAudioPlayingLottie: String

    // 电台页面播放动效
    val audioRadioPlayingLottie: String

    /**
     * 音频播放器长按倍速提示动画
     */
    val audioPlayerLongPressSpeed: String

    /**
     * aigc早报订阅音频播放Lottie
     */
    val aigcMorningPostPlayAudioLottie: String

    /**
     * 直播中
     */
    val livePlaying: String

    /**
     * 电台引导动画
     */
    val audioRadioGuide: String

    /**
     * 电台卡片长按Lottie
     */
    val audioRadioCardLongPress: String

    /**
     * 点赞Lottie
     */
    val likeLottie: String

    /**
     * 语音输入波形, 蓝色
     */
    val voiceInputBlue: String

    /**
     * 语音输入波形, 红色
     */
    val voiceInputRed: String

    /**
     * 双击点赞引导动画
     */
    val doubleTapLike: String
}
