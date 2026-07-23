package com.tencent.news.core.tads.pop.vm

import com.tencent.news.core.list.vm.IImageVM
import com.tencent.news.core.list.vm.IVideoVM
import com.tencent.news.core.tads.vm.IAdVM

interface IAdFullScreenDialogVM : IAdVM {
    val type: AdFullScreenDialogType
    val title: String
    val imgVM: IImageVM?
    val videoVM: IVideoVM?
    val showDuration: Long
}

enum class AdFullScreenDialogType {
    NONE,
    HOR_PIC,
    VER_PIC,
    HOR_VIDEO,
    VER_VIDEO
}