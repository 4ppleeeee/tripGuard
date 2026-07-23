package com.tencent.news.core.list.trace

// 【qnDetail】
object NewsDetailLog : BaseBizLog("NewsDetailLog")
object MorningPostLog : BaseBizLog("MorningPost")

// 【qnAd】
object NewsGameLog : BaseBizLog("NewsGame")

// 【qnMedia】
object NewsVideoLog : BaseBizLog("NewsVideo")
object LiveLog : BaseBizLog("NewsLive")
object AudioServiceLog : BaseBizLog("NewsAudio", subTag = "Service")
object NewsAudioLog : BaseBizLog("NewsRadio")
object AudioPodLog : BaseBizLog("AudioPod")
object AudioTabRadioLog : BaseBizLog("AudioTab3Radio")
object TTSLog : BaseBizLog("TTSLog")
object SportLog : BaseBizLog("SportLog")

// 【qnUser】
object NewsFavoriteLog : BaseBizLog("NewsFavorite")
object NewsHistoryLog : BaseBizLog("NewsHistory")
object NewsFollowLog : BaseBizLog("NewsFollow")
object NewsSubLog : BaseBizLog("NewsSub")
object NewsLikeLog : BaseBizLog("NewsLike")
object NewsSubscribeLog : BaseBizLog("NewsSubscribe")
object NewsLoginLog : BaseBizLog("NewsLogin")
object NewsEditorLog : BaseBizLog("NewsEditor")
object InterfaceVoteLog : BaseBizLog("InterfaceVote")
object UserSponsorLog : BaseBizLog("UserSponsorLog")
object NewsPayLog : BaseBizLog("NewsPay")
object NewsAigcAudioLog : BaseBizLog("AigcAudio")
object MemberRankLog : BaseBizLog("NewsPay/MemberRank")
object CheckInLog : BaseBizLog("CheckIn")
object MyHistoryLog : BaseBizLog("MyHistory")
object ProfileFansLog : BaseBizLog("ProfileFans")

// app公共日志：
object PushGuideFreqLog : BaseBizLog("PushGuideFreq")
object NewsRouterLog : BaseBizLog("NewsRouter")
object LottieLog : BaseBizLog("Lottie")
object NewsLruCache : BaseBizLog("NewsLruCache")
object NewsTimeLog : BaseBizLog("NewsTime")
object NotificationLog : BaseBizLog("Notification")
object DateLog : BaseBizLog("Date")
object EventBusLog : BaseBizLog("EventBus")
object IntentLog : BaseBizLog("Intent")
object DemoLog : BaseBizLog("Demo")
object BridgeLog : BaseBizLog("Bridge")
