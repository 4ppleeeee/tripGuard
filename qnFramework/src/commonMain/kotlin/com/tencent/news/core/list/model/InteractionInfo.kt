package com.tencent.news.core.list.model

import com.tencent.news.core.extension.IKmmKeep
import com.tencent.news.core.tads.constants.INVALID_NUM
import kotlinx.serialization.Serializable


interface IInteractionInfo : IKmmKeep {
    var wxShareCount: Int
}

@Suppress("ConstructorParameterNaming", "VariableNaming")
@Serializable
class InteractionInfo : BaseKmmModel(), IInteractionInfo, IKmmKeep {

    private var share_wechat_count: Int = INVALID_NUM

    override var wxShareCount: Int
        get() = share_wechat_count
        set(value) {
            this.share_wechat_count = value
        }
}