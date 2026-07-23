package com.tencent.news.core.view.lifecycle

sealed class ComposeEvent(val eventName: String) {

    // 系统默认回调（KuiklyRenderViewDelegator onResume, onPause 触发）
    object OnAppear : ComposeEvent("viewDidAppear")
    object OnDisappear : ComposeEvent("viewDidDisappear")

    object OnFirstFrame : ComposeEvent("pageFirstFramePaint")
    object OnDestroy : ComposeEvent("pageWillDestroy")

    object OnPageNewIntent : ComposeEvent("OnPageNewIntent")

    // 【自定义】Cell
    object Cell {
        object OnAttach : ComposeEvent("onCellAttach")
        object OnDetach : ComposeEvent("onCellDetach")
        object VisiblePercentChanged : ComposeEvent("VisiblePercentChanged") // 卡片可见面积百分比变化 [0, 100]
        object ScrollOverMid : ComposeEvent("onScrollOverMid") // 卡片滚动过屏幕中间
    }

    // 【自定义】列表
    object List {
        object OnShow : ComposeEvent("onListShow")
        object OnHide : ComposeEvent("onListHide")
        object OnDestroy : ComposeEvent("onListDestroy")
    }

    // 【自定义】数据
    object Data {
        object OnReset : ComposeEvent("OnReset")
    }

    // 【自定义】文中广告
    object AdMidArticle {
        object UpdateFontContext : ComposeEvent("updateAdMidArticleFontContext")
    }

    // 【自定义】视频回调通知
    object Video {
        object OnStart : ComposeEvent("onVideoStart")
        object OnPause : ComposeEvent("onVideoPause")
        object OnStop : ComposeEvent("onVideoStop")
        object OnProgress : ComposeEvent("onVideoProgress")
        object OnComplete : ComposeEvent("onVideoComplete")
        // 宿主投递：展示 finish-card
        object ShowFinishCover : ComposeEvent("showFinishCover")
        // 宿主投递：关闭 finish-card
        object DismissFinishCover : ComposeEvent("dismissFinishCover")
    }

    // 【自定义】微信小店横滑样式
    object WxStoreCarousel {
        // 宿主投递：用户点击图片顶部等非图片区域，请求 toggle 暂停态（与图片点击同一套逻辑）
        object HostTapPause : ComposeEvent("wxStoreCarouselHostTapPause")
    }

    // 【自定义】主动操作动画
    object Anim {
        object Enter : ComposeEvent("enterAnimation")
        object Exit : ComposeEvent("exitAnimation")
    }

    // 【自定义】音频miniBar
    object MiniBar {
        object ShrinkMiniBar : ComposeEvent("shrinkMiniBar")
        object ExpandMiniBar : ComposeEvent("expandMiniBar")
        object ShowMiniBar : ComposeEvent("showMiniBar")
        object HideMiniBar : ComposeEvent("hideMiniBar")
        object SwitchScene : ComposeEvent("switchScene")
        object CtrlJumpVisible : ComposeEvent("ctrlJumpVisible")
        object CtrlAudioPlay : ComposeEvent("ctrlAudioPlay")
    }

    // 【自定义】主动view显隐
    object Visible {
        object Show : ComposeEvent("showView")
        object Hide : ComposeEvent("hideView")
    }

    // 【自定义】 aigc相关
    object Aigc {
        object ClearSession : ComposeEvent("clearSession")
        object UpdateArticleData : ComposeEvent("UpdateArticleData")
        object NotifyNetErrorOrBackground : ComposeEvent("notifyNetErrorOrBackground")
        object NotifyMorningPostChange : ComposeEvent("NotifyMorningPostChange")
        object LossAudioFocus : ComposeEvent("LossAudioFocus")
        object MessageListSizeChanged : ComposeEvent("MessageListSizeChanged")
        object SendTextVoiceReq : ComposeEvent("sendTextVoiceReq")  // 宿主调用：发送文本转语音请求
    }

    object LifeCycle {
        object ViewWillAppear : ComposeEvent("viewWillAppear")
        object ViewDidDisappear : ComposeEvent("viewDisappear")
        object DidEnterBackground : ComposeEvent("didEnterBackground")
    }
    object Sport {
        object MatchListTabSelected : ComposeEvent("matchListTabSelected")
    }

    // 【自定义】音频相关
    object Audio {
        object RefreshRadioTab : ComposeEvent("refreshRadioTab")
        object RefreshRecordPermission : ComposeEvent("RefreshRecordPermission")
    }

    object AdCompleteOverlay {
        // 宿主投递：完播浮层显示
        object OnVideoCompleteOverlay : ComposeEvent("onVideoCompleteOverlay")
        // 宿主投递：完播浮层关闭（重播等场景）
        object OnVideoCompleteOverlayDismiss : ComposeEvent("onVideoCompleteOverlayDismiss")
    }
    object AdFeedOverlay {
        object OnVisibleRatio : ComposeEvent("onAdVisibleRatioChange")
    }

    // 【自定义】视频鉴权回调（端上 -> KMM）
    object VideoAuth {
        /** 端上鉴权结果回传（success/shouldPay/failed/WebView 控制等） */
        object OnAuthResult : ComposeEvent("onVideoAuthResult")
        /** 端上播放器生命周期转发（prepared/start/stop/complete，用于试看计时同步） */
        object OnPlayerLifecycle : ComposeEvent("onVideoPlayerLifecycle")
    }
}
