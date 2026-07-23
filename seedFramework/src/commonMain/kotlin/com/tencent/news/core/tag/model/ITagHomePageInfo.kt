package com.tencent.news.core.tag.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.list.model.IQnInterfaceCreator
import com.tencent.news.core.list.model.QnInterfaceSerializer
import kotlinx.serialization.Serializable

@Suppress("AnnotationOnSeparateLine")
typealias QnTagHomePageInfo = @Serializable(ITagHomePageInfo.QnSerializer::class) ITagHomePageInfo

interface ITagHomePageInfo : IKmmKeep {
    var nickName: String?       // 定制tag外显别名

    var lead: String?           // 导语

    var shareTitle: String?     // 分享标题
    var shareAbstract: String?  // 分享摘要
    var sharePic: String?       // 分享图片
    var shareUrl: String?       // 分享链接
    var openShare: Boolean      // 是否支持分享 默认为true

    var openSearch: Boolean     // 头部展示搜索入口
    var openDiscuss: Boolean    // 头部展示讨论区入口

    var openingAudio: String?   // 片头音频
    var endingAudio: String?    // 片尾音频
    var openingText: String?    // 片头文案
    var endingText: String?     // 片尾文案
    var switchingText: String?  // 续播文案

    val openingEndingAudio: List<IKmmOpeningEndingAudio>?    // 多音色片头片尾
    var introWords: String?     // 介绍语

    var banner724PicUrl: String?        // 724tag底层页banner url
    var banner724SchemeUrl: String?     // 724tag底层页banner 跳转scheme

    val h5Info: IKmmTagH5Info?          // 活动链接信息

    val leadPics: List<ITagPicInfo>?    // 简介图

    object QnSerializer : QnInterfaceSerializer<ITagHomePageInfo>(ITagHomePageInfo::class)

    companion object : IQnInterfaceCreator<ITagHomePageInfo> {
        override fun defaultSerializer() = QnSerializer
    }
}