package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.isNotNullOrEmpty
import com.tencent.news.core.tads.constants.AdLoid


data class AdScene constructor(
    val majorLoid: Int,
    val adChannel: String,
) {

    fun isValid(): Boolean {
        return majorLoid != AdLoid.NONE && adChannel.isNotNullOrEmpty()
    }

    fun getKey(): String {
        return "${adChannel}/loid[${majorLoid}]"
    }

}