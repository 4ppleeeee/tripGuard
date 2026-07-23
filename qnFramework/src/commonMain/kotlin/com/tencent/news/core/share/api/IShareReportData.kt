package com.tencent.news.core.share.api

interface IShareReportData {
    val hostUrl: String
    val params: Map<String, String?>
    val requireLogin: Boolean
}
