package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc
import com.tencent.news.core.tads.constants.AdBrokenInfoType


interface IAdOrderRes : IAdOrderDtoDoc {

    var videoId: String     // 视频vid（需换链播放）
    var videoUrl: String    // 视频url（可直接播放）
    val soundTrackUrl: String // 直购横滑音轨视频url（sound_track_url）
    var video2Url: String   // 备用视频url（目前用于超级蒙层的破窗视频url）
    var videoDuration: Long // 视频时长（单位 s）

    val superMaskImageUrl: String   // 超级蒙层图片资源
    val superMaskButtonUrl: String  // 超级蒙层按钮资源
    val superMask: IAdSuperMask?    // 超级蒙层配置
    var sdtFrom: String     // 视频带宽计费渠道
    val originVid: String    // 原始的播放vid（竖转横）
    val originImageUrl: String     // 原始的视频素材封面图（竖转横）

    var imgRes0: String     // 订单图片素材地址，例如：视频信息流还表示封面素材
    var imgRes1: String     // 备用素材地址，例如：闪屏H5、GIF图片
    var imgRes2: String     // 备用素材地址2，例如：精选冠名广告位图片
    val webpList: List<IAdWebpItem>? // WebP 资源列表

    val paletteColor: String        // 素材吸色

    var shareImg: String            // 分享缩略图

    val lottieUrl: String

    val gifPackageSize: Int // GIF包的大小

    val videoInfo: IKmmAdVideoInfo?  // 视频基本数据
    val liveInfo: IAdLiveInfo?   // 直播数据
    val liveQueryUrl: String        // 直播间商品轮询链接
    var liveProducts: List<IAdWxLiveProductInfo>? // 直播商品信息

    val fullScreenDialogInfo: IAdFullScreenDialogInfo?

    val resItemList: List<IAdResItem>
    var resUrlList: List<String>
    var resTitleList: List<String>
    var resClickUrlList: List<String>

    var imgW: Int
    var imgH: Int

    var logoPicRatio: Float // 随播广告icon的比例

    val richMediaUrl: String
    val richMediaType: Int

    var extendMaterial: IAdExtendMaterial?

    var subOrders: List<IKmmAdOrder>?       // 子订单与父订单数据合并后的
    val originSubOrders: List<IKmmAdOrder>? // 后台下发的原始子订单数据（数据不全）

    var isPortraitVideo: Boolean            // 是否为竖版视频，谨慎重写

    var isPreloadWebRes: Boolean            //  是否走spa预加载落地页资源方案.

    var hotIcon: String                     // 焦点图广告文案

    val categoryIcon: String                // 标题前的tag

    fun getBrokenInfoUrl(type: AdBrokenInfoType): String? // 奥运画廊-破窗挂件

}
