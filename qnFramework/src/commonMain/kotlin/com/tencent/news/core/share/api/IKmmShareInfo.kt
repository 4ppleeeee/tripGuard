package com.tencent.news.core.share.api


/**
 * Author: joejhzhou
 * Date: 2024/12/17
 **/

interface IKmmShareInfo {
    val title: String?
    val description: String?
    val url: String?
    val images: List<String>
}


interface IKmmShareInfoProvider {
    val adShareInfo: IKmmShareInfo?
    val shareDocInfo: IKmmShareInfo?
    val shareDtoInfo: IKmmShareInfo?
    val shareItemInfo: IKmmShareInfo?
    val commentShareInfo: IKmmShareInfo?
    val newsDetailShareInfo: IKmmShareInfo?
}