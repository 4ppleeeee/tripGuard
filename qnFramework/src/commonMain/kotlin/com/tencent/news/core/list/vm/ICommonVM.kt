package com.tencent.news.core.list.vm

import com.tencent.news.core.app.constants.IconFont
import com.tencent.news.core.page.model.StructBg
import com.tencent.news.core.page.model.StructColor
import com.tencent.news.core.page.model.StructSize


typealias ClickAction = () -> Unit
typealias ReportAction = () -> Unit
typealias AttachAction = () -> Unit
typealias DetachAction = () -> Unit


interface IViewVM : IStructSize, IStructBg, IExposeVM

interface IImageVM : IViewVM {
    val imgUrl: String
    val nightImgUrl: String
}

interface IVideoVM {
    val vid: String
    val videoUrl: String
    val videoDuration: Long
    val coverImgVM: IImageVM?
}

interface IBtnVM : IClickVM, IViewVM {
    val btnText: String
    val btnTextSelected: String
    val isBtnSelected: Boolean // 按钮被选中（影响文案和样式）
    val textColor: StructColor?
    val textSize: Float

    val rightIconFont: IconFont?
    val leftIconFont: IconFont?
}

interface IClickVM {
    val clickUrl: String
    var clickAction: ClickAction?
    var clickReport: ReportAction?
    val beforeClick: List<ClickAction>?
    val afterClick: List<ClickAction>?
}

interface IExposeVM {
    val exposeAction: ClickAction?
    val exposeReport: ReportAction?
}

interface IStructSize {
    val size: StructSize?
}

interface IStructBg {
    val bg: StructBg?
}
interface IViewLifeCycleVM {
    var attachAction: AttachAction?
    var detachAction: DetachAction?
}

interface IImageBtnVM : IImageVM, IBtnVM
