package com.tencent.news.core.channel.config

import com.tencent.news.core.extension.IConfigDoc
import com.tencent.news.core.extension.getJsonStr
import com.tencent.news.core.extension.toJsonElement
import com.tencent.news.core.platform.api.getShiplyStringList
import com.tencent.news.core.platform.api.urlEncode


object ChannelConfig : IConfigDoc {

    private val financialZonChannelList by lazy {
        getShiplyStringList(
            "financial_zone_channel_list",
            listOf("news_news_secu", "news_news_loan")
        )
    }

    internal val fullScreenChannelList by lazy {
        getShiplyStringList("full_screen_channel_list") ?: emptyList()
    }

    fun getFinancialZonePageScheme(): String {
        val jumpInfoJson = mapOf(
            "tab_id" to "local_group_tab_secu",
            "channel_list" to financialZonChannelList?.joinToString(",")
        ).toJsonElement().getJsonStr()

        return "qqnews://article_9527?nm=NEWSJUMP_90061&jumpinfo=${urlEncode(jumpInfoJson)}"
    }

}