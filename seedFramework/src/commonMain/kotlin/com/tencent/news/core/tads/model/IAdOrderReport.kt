package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc


interface IAdOrderReport : IAdOrderDtoDoc {

    var viewReportUrl: String       // 曝光上报
    var feedbackReportUrl: String   // 负反馈上报
    var downloadReportUrl: String   // 下载链路效果上报（目前后台这个值填充的和effectReportUrl一致的，不特殊区分下载了）
    var effectReportUrl: String     // 效果上报
    var extraReportUrl: String      // 额外效果上报（图片/落地页加载等）
    var videoReportUrl: String      // 视频播放上报
    var interactiveUrl: String      // 互动上报
    var complaintUrl: String        // 投诉url.
    val impStayReportUrl: String    // 广告停留时长上报链接
    val viewId: String              // 广点通订单每次展示唯一的id，主要用于互动上报

    val gdtClickData: String        // 后台透传字段，点击时带上
    val gdtPingData: String         // 后台透传字段，包含计价等信息，曝光时带上

    var enableAsyncClick: Boolean   // 是否开启异步上报

    val semiSubType: Int            // 订单展示样式 ssp上报时使用

    val reportUrlSdk: List<IAdThirdReportInfo>?
    val reportUrlOther: List<IAdThirdReportInfo>?
    val clickReportUrlOther: List<IAdThirdReportInfo>?

    val thirdApiExposureUrls: List<String>                // 过滤后的曝光 API 三方 URL 列表，
    val thirdSdkExposureReports: List<IAdThirdReportInfo> // 过滤后的曝光 SDK 三方上报 item 列表
    val thirdApiClickUrls: List<String>                   // 过滤后的点击 API 三方 URL 列表（仅 reportType==API）
    val thirdSdkClickReports: List<IAdThirdReportInfo>    // 过滤后的点击 SDK 三方上报 item 列表（reportType!=API）

    val reportPlaySecs: List<IAdVideoReportInfo>?

    fun setExposureApiUrls(urls: List<String>?)
    fun setExposureSdkUrls(urls: List<String>?)
    fun setClickApiUrls(urls: List<String>?)
    fun setClickSdkUrls(urls: List<String>?)

    fun clearThirdExposureReports()
    fun clearThirdClickReports()

}