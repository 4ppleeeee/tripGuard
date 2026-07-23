package com.tencent.news.core.parcel


interface IResetAfterClone : IDtoHolderItem, IVMHolderItem

interface IDtoHolderItem {
    fun resetAllDto()
}

interface IVMHolderItem {
    fun resetAllVM()
}

fun IResetAfterClone?.clearAfterClone() {
    this ?: return
    resetAllDto()
    resetAllVM()
}