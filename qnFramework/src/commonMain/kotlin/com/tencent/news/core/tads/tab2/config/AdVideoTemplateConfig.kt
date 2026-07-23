package com.tencent.news.core.tads.tab2.config

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tads.tab2.AdIndustryResConfig
import kotlinx.serialization.Serializable

@Serializable
data class AdVideoTemplateConfig(
    val l1: List<AdVideoCardConfig> = listOf(),
    val l2: List<AdVideoCardConfig> = listOf(),
    val l3: List<AdVideoCardConfig> = listOf(),
    val l4: List<AdVideoCardConfig> = listOf(),
    val resConfig: AdIndustryResConfig? = null,
) : IKmmKeep {

    var styleKey: String = "Style-Key"

    fun isValid(): Boolean = l1.isValid() || l2.isValid() || l3.isValid() || l4.isValid()

    private fun List<AdVideoCardConfig>.isValid(): Boolean = any { it.isValid() }

    fun isMatchStyle(styleKey: String): Boolean {
        return this.styleKey == styleKey
    }

}