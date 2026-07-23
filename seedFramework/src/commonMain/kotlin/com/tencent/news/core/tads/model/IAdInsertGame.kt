@file:Suppress("MaxLineLength")

package com.tencent.news.core.tads.model

interface IAdInsertGame {

    // 云游戏暂不支持，预留：
//    var cloud_play_url: String? = null // 【必填】云游戏h5版本的跳转链接（当sdk参数非法时，兜底跳转这个）
//    var permission_url: String? = null // 【必填】云游戏隐私条约（启动云游戏前的合规弹窗用到）
//    var sdk_cloud_game_id: String? = null // 【必填】云游戏sdk gameid
//    var qq_app_id: String? = null // 【必填】云游戏sdk qqAppId
//    var wx_app_id: String? = null // 【必填】云游戏sdk wxAppId

    val eventTitle: String      // 活动标题（如果下发则优先展示）
    val eventClickUrl: String   // 活动跳转链接（如果下发则优先跳转）

    val gameName: String        // 游戏名称：英雄联盟手游
    val gameScore: String       // 游戏评分：4.7
    val giftTotalNum: String    // 礼包数量：10
    val gameIcon: String        // 游戏图标：https://h5.ssp.qq.com/new_webp_lianyun/202111/20211122094734_2430.png
    val gameDesc: String        // 游戏简介：风靡全球的MOBA经典之作
    val iosUrl: String          // iOS下载链接：https://itunes.apple.com/cn/app/id1455054000"
    val androidUrl: String      // 安卓下载链接：http://imtt.dd.qq.com/sjy.20005/16891/apk/CD36DCFB63841C733F8EF8D511AD3DCE.apk?fsname=com.tencent.lolm_553164.apk&csr=a526
    val jumpUrl: String         // banner配置的url：https://h5.ssp.qq.com/lps/production/material/202507/31970989f9baf5fc04b25f7266a074f4.html?sourceType=2&channelIndex=2
    val gameId: String          // 游戏id：77302

    val matchType: String       // 1
    val materialType: String    // cms_ad 广告，game 游戏

    val locationIndex: String   // 广告在底层页正文插入的位置：391
    val postCheckStr: String    // 插入位置的前若干字符，终端检验用：<P>2.小乔</P>
    val preCheckStr: String     // 插入位置的后若干字符，终端检验用：<P><!--IMG_1--></P>

    val respId: String          // 2216bd104b473ca9c6efb752072b

    val serverData: String      // 服务端透传数据（含联运实验id等），曝光上报时透传

    val msgId: Int                       // 通知ID，用于上报
    val msgDesc: String                  // 通知标题
    val msgImg: String                   // 通知图片URL
    val msgUrl: String                   // 点击跳转链接
    val msgBtnText: String               // 按钮文案
    val msgTitle: String                 // 副标题
}