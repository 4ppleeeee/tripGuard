package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc
import com.tencent.news.core.tads.constants.AdJumpAction
import com.tencent.news.core.tads.constants.AdJumpLinkInfoData
import com.tencent.news.core.tads.constants.AdJumpLinkMap
import com.tencent.news.core.tads.constants.AdNativePosType


interface IAdOrderAction : IAdOrderDtoDoc {

    var landingUrl: String          // 落地页地址（换链之后会替换这个url，变成落地页纯h5链接，不带gdt上报）
    val originLandingUrl: String    // 落地页地址（换链之前的原始url，带gdt上报，禁止修改）

    var destUrl: String             // 广告主原始url（不带计费上报的url）支持异步点击上报时，这个会提前在信息流下发
    var wechatCanvasInfo: String    // 微信原生页画布信息（xj_wechat_canvas_info）
    var clickId: String             // 广告计费点击上报id（配合 destUrl 做异步上报时使用）；主要就外链类型有用，其余没啥用了
    val originClickId: String       // 未经修改的原始clickId（格式类似：k7ds42e2aiabjtlg3wnq__CNT__）

    var jumpActions: List<AdJumpAction>?            // 双链/三链跳转
    var jumpLinkMap: AdJumpLinkMap?                 // PCAD 2.0链路：跳转行为
    var jumpLinkData: List<AdJumpLinkInfoData>?     // PCAD 2.0链路：跳转数据

    var openScheme: String                  // 外跳scheme
    var openPkg: String                     // 外跳包名（不一定有）
    var openPkgAlternate: String            // 备用应用包名，用于落地页智能直达中候补包名.

    val wxDirectLink: String                 // 微粒贷跳转地址，传入微信SDK

    var wxMiniProgram: IWxMiniProgram?      // 小程序/小游戏 信息
    val miniGameInfo: IAdMiniGameInfo?      // 信息流广告微信小游戏信息
    var formComponent: AdFormComponentInfo? // 表单组件（电话、咨询、表单）
    var appointData: AdAppointData?         // 预约大卡
    val quickJumpInfo: IAdQuickJumpInfo?    // 快应用跳转信息
    var actionBtn: AdActionBtn?             // 行动按钮文案（目前仅跳转微信原生页使用到）
    val adHalfScreenCardInfo: AdHalfScreenCardInfo? // 半屏卡信息
    var wxNativeExtInfo: String             // 微信原生页跳转ExtInfo透传字段
    val miniGameManuscriptUrl: String       // 跳转小游戏Url

    var hideAdIcon: Boolean         // 隐藏广告标
    var iconText: String            // 广告标文案
    val adLabels: List<IAdLabel>?   // 广告标签（游戏、客服类订单会用）

    var canShare: Boolean           // 是否可以分享广告（广告落地页右上角分享按钮、tab2分享按钮）

    var autoReplay: Boolean         // 视频播放完毕后自动重播

    val nativeStyleType: String     // 行业样式判断类型

    val clickHotArea: IAdClickHotArea?

    val hideAdTitle: Boolean       // 隐藏标题

    var dynamicMosaicPageTemplateId: String  // 动态化原生页模版 ID

    var destUrlReflectionId: String //

    var universalLink: String // iOS专用跳转链接

    var isSoundOpen: Boolean  // 是否打开声音

    var enableLandscape: Boolean // 是否支持落地页横屏展示.

    var hideComplaint: Boolean    // 是否隐藏落地页投诉按钮.

    var brandIcon: String       // 品牌icon.

    val freqCount: Int          // 广告频控

    var enableShowReconfirmDialog: Boolean  // 是否展示落地页挽留弹窗

    val newStyle: Int  // 新样式版本，3-代表第3改版

    var isWechatWhitelist: Boolean // 微信原生页是否在白名单中

    val isBlockTopVideo: Boolean // 落地页顶部视频禁用（客户端、官方页、蹊径h5 的拼接都关掉）

    val nativeAdCmsIdStr: String // 原生广告匹配内容素材cmsId
    val isNativePosAd: Boolean   // 是否是原生广告槽位

    var nativePosType: AdNativePosType  // 标识当前原生槽位类型，只做server透传，不表示实际样式

    val isConsultDisplay: Boolean      // 是否是客服特殊特殊标识（官方页）

    var newsId: String            // 落地页可评论广告的伪造新闻id.

    var uxinfo: String            // uxinfo 上报字段

}
