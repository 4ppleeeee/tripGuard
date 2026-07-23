@file:Suppress("RedundantConstructorKeyword", "unused")

package com.tencent.news.core.tads.click

data class AdReportConfig constructor(
    val reportGdtClick: Boolean = true,     // gdt计费点击
    val reportSspClick: Boolean = true,     // ssp点击
    val reportLinkClick: Boolean = false,   // ams链路上报：一般不在这里报，是在跳转链路处理里报的
) {

    companion object {
        fun all() = AdReportConfig(
            reportGdtClick = true,
            reportSspClick = true,
            reportLinkClick = true,
        )

        fun onlyGdt() = AdReportConfig(
            reportGdtClick = true,
            reportSspClick = false,
            reportLinkClick = false,
        )

        fun onlySsp() = AdReportConfig(
            reportGdtClick = false,
            reportSspClick = true,
            reportLinkClick = false,
        )

        fun onlyLink() = AdReportConfig(
            reportGdtClick = false,
            reportSspClick = false,
            reportLinkClick = true,
        )
    }

}