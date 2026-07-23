package com.tencent.news.core.tads.model

import com.tencent.news.core.extension.IKmmKeep
import kotlinx.serialization.Serializable


@Suppress("PrivatePropertyName")

@Serializable
class AdHalfScreenCardInfo : IKmmKeep {

    /** 半屏卡类型 0 位置 1 半屏咨询卡 */
    private val half_screen_card_type: Int = 0

    /** 咨询文案*/
    private val consulting_text: String = ""

    /** 更多文案 */
    private val additional_text: String = ""

    private val link_template_id: String = ""

    /** 问题集合*/
    private val consulting_question: List<String> = emptyList()

    /** 问题跳转 Url*/
    private val consulting_question_dest_urls: List<String> = emptyList()

    /** 是否支持自动发送问题，由链路配置平台根据微信版本等信息配置下发*/
    private val support_auto_send_question: Boolean = false


    val halfScreenCardType: Int
        get() = half_screen_card_type

    val consultingText: String
        get() = consulting_text

    val additionalText: String
        get() = additional_text

    val linkTemplateId: String
        get() = link_template_id

    val consultingQuestionList: List<String>
        get() = consulting_question

    val consultingQuestionDestUrls: List<String>
        get() = consulting_question_dest_urls

    val supportAutoSendQuestion: Boolean
        get() = support_auto_send_question
}