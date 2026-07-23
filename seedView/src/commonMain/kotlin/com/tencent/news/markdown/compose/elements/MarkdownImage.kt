package com.tencent.news.markdown.compose.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.layout.boundsInRoot
import com.tencent.kuikly.compose.ui.layout.onGloballyPositioned
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.markdown.utils.findChildOfTypeRecursive
import com.tencent.news.core.compose.view.markdown.utils.getUnescapedTextInNode
import com.tencent.news.markdown.compose.LocalImageLinkHandler
import com.tencent.news.markdown.compose.LocalImageTransformer
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode

@Composable
internal fun MarkdownImage(content: String, node: ASTNode) {

    val link =
        node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)?.getUnescapedTextInNode(content) ?: return
    val imageHandler = LocalImageLinkHandler.current
    val frameState = remember { mutableStateOf(Rect.Zero) }
    val density = LocalDensity.current

    LocalImageTransformer.current.transform(link, null, false)?.let { imageData ->
        imageHandler.store(imageData)
        
        QnImage(
            painter = imageData.painter,
            contentDescription = imageData.contentDescription,
            modifier = Modifier
                .clickable {
                    imageHandler.preview(imageData, link, frameState.value, density)
                }
                .then(imageData.modifier)
                .onGloballyPositioned {
                    frameState.value = it.boundsInRoot()
                },
            alignment = imageData.alignment,
            contentScale = imageData.contentScale,
            alpha = imageData.alpha,
            colorFilter = imageData.colorFilter,
        )
    }
}



