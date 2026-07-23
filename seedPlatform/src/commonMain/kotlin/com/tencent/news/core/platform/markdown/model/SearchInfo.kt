package com.tencent.news.core.markdown.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable

@Serializable
class SearchInfo : IKmmKeep {
    private val search_results: List<SearchItem?>? = null

    val searchResults: List<SearchItem?>?
        get() = search_results

    fun getSearchItems(index: List<String>): List<SearchItem>? {
        return searchResults
            ?.filter { index.contains(it?.index.toString()) }
            ?.filterNotNull()
    }

}

@Serializable
class SearchItem : IKmmKeep {
    val index: Int = 0
    val title: String? = null
    val url: String? = null

}