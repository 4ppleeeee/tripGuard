package com.tencent.news.core.list.vm

import com.tencent.news.core.app.IKmmContext
import com.tencent.news.core.app.LocalKmmContext
import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.page.model.StructBg
import com.tencent.news.core.page.model.StructColor
import com.tencent.news.core.page.model.StructSize
import com.tencent.news.core.platform.api.appRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


data class BtnVM(
    override val btnText: String = "",
    override val btnTextSelected: String = btnText,
    override val isBtnSelected: Boolean = false,
    override val textColor: StructColor? = null,
    override val textSize: Float = 0f,

    override val rightIconFont: IconFont? = null,
    override val leftIconFont: IconFont? = null,

    override val clickUrl: String = "",
    override var clickAction: ClickAction? = null,
    override var clickReport: ReportAction? = null,
    override val beforeClick: List<ClickAction>? = null,
    override val afterClick: List<ClickAction>? = null,

    override val exposeAction: ClickAction? = null,
    override val exposeReport: ReportAction? = null,

    override val size: StructSize? = null, // 整个按钮的尺寸
    override val bg: StructBg? = null,
) : IBtnVM

data class ClickVM(
    override val clickUrl: String = "",
    override var clickAction: ClickAction? = null,
    override var clickReport: ReportAction? = null,
    override val beforeClick: List<ClickAction>? = null,
    override val afterClick: List<ClickAction>? = null,
) : IClickVM

fun IClickVM?.createValidAction(
    context: IKmmContext? = null,
    replaceClickAction: ClickAction? = null,        // 允许外部替换点击行为
): ClickAction? {
    val vm = this ?: return null
    return replaceClickAction
        ?: vm.clickAction                           // vm设置的点击行为
        ?: vm.clickUrl.createSchemeAction(context)  // scheme跳转
}

fun IClickVM?.runAll(clickAction: ClickAction, hookClick: Boolean = false) {
    this ?: return

    beforeClick?.forEach { it.invoke() }

    if (!hookClick) {
        clickAction()
    }

    clickReport?.invoke()
    afterClick?.forEach { it.invoke() }
}

private fun String?.createSchemeAction(context: IKmmContext?): ClickAction? {
    if (this.isNullOrBlank()) {
        return null
    }
    val scheme = this
    return {
        CoroutineScope(Dispatchers.Main).launch {
            appRouter().to(context ?: LocalKmmContext, scheme)
        }
    }
}
