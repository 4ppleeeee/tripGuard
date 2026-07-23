package com.tencent.kmm.demo.startup.sdk.kuikly

import android.content.Context
import com.tencent.kuikly.core.render.android.IKuiklyRenderContext
import com.tencent.kuikly.core.render.android.adapter.IKRTextPostProcessorAdapter
import com.tencent.kuikly.core.render.android.adapter.TextPostProcessorInput
import com.tencent.kuikly.core.render.android.adapter.TextPostProcessorOutput

/**
 * 底座默认文本后处理器。
 * 业务 core 如需表情短码等能力，可提供自己的 [IKRTextPostProcessorAdapter]。
 */
class KRTextPostProcessorAdapter(private val context: Context) : IKRTextPostProcessorAdapter {

    override fun onTextPostProcess(inputParams: TextPostProcessorInput): TextPostProcessorOutput {
        return onTextPostProcess(null, inputParams)
    }

    override fun onTextPostProcess(
        kuiklyRenderContext: IKuiklyRenderContext?,
        inputParams: TextPostProcessorInput,
    ): TextPostProcessorOutput {
        return TextPostProcessorOutput(inputParams.sourceText ?: "")
    }
}
