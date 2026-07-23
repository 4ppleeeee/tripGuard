package com.tencent.news.core.tads.model

import com.tencent.news.core.list.model.BaseKmmModel
import kotlinx.serialization.Serializable


@Serializable
class AdActionBtn : BaseKmmModel() {

    val text: String = ""

    private val text_color: String = ""
    fun getTextColor(): String {
        return text_color
    }

    private val background_color: String = ""

    fun getBackgroundColor(): String {
        return background_color
    }

}