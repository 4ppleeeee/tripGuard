package com.tencent.news.core.tads.pop.vm

interface IAdClickFrameVM {
    val top: Int
    val bottom: Int
    val left: Int
    val right: Int

    fun onClick()
}