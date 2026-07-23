package com.tencent.news.core.list.vm

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.page.model.StructBg
import com.tencent.news.core.page.model.StructColor
import com.tencent.news.core.page.model.StructSize


data class ImageVM(
    override val imgUrl: String = "",
    override val nightImgUrl: String = imgUrl,

    override val exposeAction: ClickAction? = null,
    override val exposeReport: ReportAction? = null,

    override val size: StructSize? = null,
    override val bg: StructBg? = null,
) : IImageVM

data class ImageBtnVM(
    override val imgUrl: String = "",
    override val nightImgUrl: String = imgUrl,

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

    override val size: StructSize? = null,
    override val bg: StructBg? = null,
) : IImageBtnVM


data class VideoVM(
    override val vid: String = "",
    override val videoUrl: String = "",
    override val videoDuration: Long = 0,
    override val coverImgVM: IImageVM? = null,
) : IVideoVM