package com.tencent.news.core.tads.model

import com.tencent.news.core.list.api.IExportModelData
import com.tencent.news.core.tads.discovery.IAdDiscoveryVM
import com.tencent.news.core.tads.feeds.vm.IAdVideoVM
import com.tencent.news.core.tads.vm.IAdActionBtnVM
import com.tencent.news.core.tads.vm.IAdBottomBarVM
import com.tencent.news.core.tads.vm.IAdDebugMsgVM
import com.tencent.news.core.tads.vm.IAdDspVM
import com.tencent.news.core.tads.vm.IAdFakeVoteVM
import com.tencent.news.core.tads.vm.IAdFeedbackBtnVM
import com.tencent.news.core.tads.vm.IAdIconVM
import com.tencent.news.core.tads.vm.IAdImageCoverVM
import com.tencent.news.core.tads.vm.IAdLabelVM
import com.tencent.news.core.tads.vm.IAdMainTitleVM
import com.tencent.news.core.tads.vm.IAdMultiImageVM
import com.tencent.news.core.tads.vm.IAdStoreIconVM
import com.tencent.news.core.tads.vm.IAdvertiserVM
import com.tencent.news.core.tads.vm.VMHolder
import com.tencent.news.core.tads.vm.VMHolder2


typealias ActionBtnChecker = () -> Boolean

// 广告基础vm，所有场景都可以公用的
interface IKmmAdVM : IExportModelData {
    val dsp: VMHolder2<IAdDspVM>                    // 广告来源标签
    val adIcon: VMHolder2<IAdIconVM>                // 广告标
    val mainTitle: VMHolder2<IAdMainTitleVM>        // 主标题
    val imageCover: VMHolder2<IAdImageCoverVM>      // 封面图
    val multiImage: VMHolder<IAdMultiImageVM>       // 多图
    val labels: VMHolder2<IAdLabelVM>               // 小灰字标签
    val actionBtn: VMHolder2<IAdActionBtnVM>        // 行动按钮
    val advertiser: VMHolder2<IAdvertiserVM>        // 广告主头像
    val feedbackBtn: VMHolder2<IAdFeedbackBtnVM>    // 负反馈按钮
    val video: VMHolder<IAdVideoVM>                 // 视频播放

    val storeIcon: VMHolder<IAdStoreIconVM>          // 微信小店标识（好店/R标）
    val bottomBar: VMHolder2<IAdBottomBarVM>         // 广告底栏

    val fakeVoteVM: VMHolder2<IAdFakeVoteVM>        // 随机点赞数
    val debugMsg: VMHolder2<IAdDebugMsgVM>          // 【debug】红字调试信息

    // 发现频道VM todo torreszhang opt: 这个不该放这里
    val discovery: VMHolder2<IAdDiscoveryVM>

}
