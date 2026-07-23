package com.tencent.news.markdown.model

import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.unit.Density

interface ImageLinkHandler {

    val stored: MutableList<ImageData>

    fun store(image: ImageData)

    fun preview(image: ImageData, imageUrl: String, actualFrame: Rect, density: Density)
}

open class ImageLinkHandlerImpl : ImageLinkHandler {

    override val stored: MutableList<ImageData> = mutableListOf()

    override fun store(image: ImageData) {
        stored.add(image)
    }

    override fun preview(image: ImageData, imageUrl: String, actualFrame: Rect, density: Density) {
    }
}