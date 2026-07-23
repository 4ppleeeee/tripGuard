package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IAdOrderDtoDoc


interface IAdOrderDownloadDto : IAdOrderDtoDoc {

    var isGdtDownload: Boolean

    var pkgUrl: String      // 下载链接（老字段都已废弃，目前只是 app_download_url）
    var pkgName: String     // 下载app的包名，例如：com.achievo.vipshop
    var pkgVersion: Int     // 下载app的版本号，例如：93506

    val androidDownloadStyle: Int           // 安卓下载页展示形态 @AdAndroidDownloadStyle

    val appChannelInfo: IAppChannelInfo?    // 下载十要素

    var appName: String
    var appLogoUrl: String     // 应用图标
    val appScore: Int
    val downloadScore: Double
    val downloadNum: Long
    val advertiserClickNum: Long
    val androidAppCategoryName: String
    val iosAppCategoryNameList: List<String>

    // iOS专用字段
    var appId: String?
    var urlIOS: String?

    var pkgSize: Long            // 包大小
    var autoInstall: Boolean      // 是否自动安装
    var pkgEditorIntro: String    // 编辑推荐语

    val disableJumpAppHome: Boolean // 应用已经安装时，点击跳转到H5落地页

    var adAndroidJumpMarket: AdAndroidJumpMarket?   // android跳厂商商店数据

    fun isAppIdValid(): Boolean;


}
