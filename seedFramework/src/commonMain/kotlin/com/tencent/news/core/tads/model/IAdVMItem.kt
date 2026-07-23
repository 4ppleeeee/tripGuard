package com.tencent.news.core.tads.model

import com.tencent.news.core.tads.articles.IAdArticleVMHolder
import com.tencent.news.core.tads.feeds.vm.IAdFeedsVMHolder
import com.tencent.news.core.tads.pendant.vm.IAdPendantVMHolder
import com.tencent.news.core.tads.pop.vm.IAdPopUpVMHolder
import com.tencent.news.core.tads.tab2.vm.IAdVideoVMHolder
import com.tencent.news.core.view.ILogicContextHolder
import com.tencent.news.core.vm.IAdVMItemStub


interface IAdVMItem : IAdVMItemStub, ILogicContextHolder {
    val vm: IKmmAdVM                            // 基础信息
    val adFeedsVM: IAdFeedsVMHolder             // 信息流
    val adPopUpVM: IAdPopUpVMHolder             // 弹窗
    val adPendantVM: IAdPendantVMHolder         // 挂件
    val videoVM: IAdVideoVMHolder               // 视频模块（含tab2）
    val articleMidVM: IAdArticleVMHolder        // 图文模块
}