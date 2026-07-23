package com.tencent.news.markdown.compose.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.foundation.clickable
import com.tencent.kuikly.compose.foundation.text.InlineTextContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.geometry.Rect
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.layout.boundsInRoot
import com.tencent.kuikly.compose.ui.layout.onGloballyPositioned
import com.tencent.kuikly.compose.ui.layout.onPlaced
import com.tencent.kuikly.compose.ui.platform.LocalDensity
import com.tencent.kuikly.compose.ui.text.AnnotatedString
import com.tencent.kuikly.compose.ui.text.Placeholder
import com.tencent.kuikly.compose.ui.text.TextLayoutResult
import com.tencent.kuikly.compose.ui.text.TextStyle
import com.tencent.kuikly.compose.ui.text.buildAnnotatedString
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.isSpecified
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.base.Size
import com.tencent.news.core.compose.view.QnImage
import com.tencent.news.core.compose.view.markdown.utils.MARKDOWN_TAG_IMAGE_URL
import com.tencent.news.markdown.annotator.AnnotatorSettings
import com.tencent.news.markdown.annotator.annotatorSettings
import com.tencent.news.markdown.annotator.buildMarkdownAnnotatedString
import com.tencent.news.markdown.compose.LocalImageLinkHandler
import com.tencent.news.markdown.compose.LocalImageTransformer
import com.tencent.news.markdown.compose.LocalMarkdownAnimations
import com.tencent.news.markdown.compose.LocalMarkdownColors
import com.tencent.news.markdown.compose.LocalMarkdownExtendedSpans
import com.tencent.news.markdown.compose.LocalMarkdownTypography
import com.tencent.news.markdown.compose.elements.material.MarkdownBasicText
import com.tencent.news.markdown.compose.extendedspans.ExtendedSpans
import com.tencent.news.markdown.compose.extendedspans.drawBehind
import com.tencent.news.markdown.model.ImageTransformer
import com.tencent.news.markdown.model.PlaceholderConfig
import com.tencent.news.markdown.model.rememberMarkdownImageState
import org.intellij.markdown.IElementType
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.findChildOfType


@Composable
fun MarkdownText(
    content: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalMarkdownTypography.current.text,
    annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
    MarkdownText(AnnotatedString(content), modifier, style, annotatorSettings = annotatorSettings)
}

@Composable
internal fun MarkdownText(
    content: String,
    node: ASTNode,
    style: TextStyle,
    modifier: Modifier = Modifier,
    contentChildType: IElementType? = null,
    annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
    val childNode = contentChildType?.run(node::findChildOfType) ?: node
    val styledText = buildAnnotatedString {
        pushStyle(style.toSpanStyle())
        buildMarkdownAnnotatedString(
            content = content,
            node = childNode,
            annotatorSettings = annotatorSettings
        )
        pop()
    }

    MarkdownText(
        styledText,
        modifier = modifier,
        style = style,
        annotatorSettings = annotatorSettings
    )
}

@Composable
fun MarkdownText(
    content: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalMarkdownTypography.current.text,
    extendedSpans: ExtendedSpans? = LocalMarkdownExtendedSpans.current.extendedSpans?.invoke(),
    annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
    // extend the annotated string with `extended-spans` styles if provided
    val extendedStyledText = if (extendedSpans != null) {
        remember(content) {
            extendedSpans.extend(content)
        }
    } else {
        content
    }

    // forward the `onTextLayout` to `extended-spans` if provided
    val onTextLayout: ((TextLayoutResult, Color?) -> Unit)? = if (extendedSpans != null) {
        { layoutResult, color ->
            extendedSpans.onTextLayout(layoutResult, color)
        }
    } else {
        null
    }

    // call drawBehind with the `extended-spans` if provided
    val extendedModifier = if (extendedSpans != null) {
        modifier.drawBehind(extendedSpans)
    } else modifier

    MarkdownText(
        content = extendedStyledText,
        modifier = extendedModifier,
        style = style,
        onTextLayout = onTextLayout
    )
}

@Composable
internal fun MarkdownText(
    content: AnnotatedString,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    style: TextStyle = LocalMarkdownTypography.current.text,
    onTextLayout: ((TextLayoutResult, Color?) -> Unit)?,
    annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
    val baseColor = LocalMarkdownColors.current.text
    val animations = LocalMarkdownAnimations.current
    val transformer = LocalImageTransformer.current

    val layoutResult: MutableState<TextLayoutResult?> = remember { mutableStateOf(null) }
    val imageState = rememberMarkdownImageState()

    val placeholderState by remember(imageState) {
        derivedStateOf {
            transformer.placeholderConfig(imageState.containerSize, imageState.intrinsicImageSize)
        }
    }

    val inlineContent = mutableMapOf(
        MARKDOWN_TAG_IMAGE_URL to createImageInlineTextContent(placeholderState, transformer)
    )
    val extendedInlineContent: Map<String, InlineTextContent>? =
        annotatorSettings.annotator.inlineContent?.invoke()
    if (extendedInlineContent != null) {
        inlineContent.putAll(extendedInlineContent)
    }

    MarkdownBasicText(
        text = content,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
            .onPlaced { it ->
                val size =
                    if (style.lineHeight.isSpecified) style.lineHeight.value else it.size.height
                imageState.updateContainerSize(Size(size.toFloat(), size.toFloat()))
            }
            .also {
                @Suppress("DEPRECATION")
                if (placeholderState.animate) animations.animateTextSize(it) else it
            },
        style = style,
        inlineContent = inlineContent,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout?.invoke(it, baseColor)
        }
    )
}

@Composable
internal fun createImageInlineTextContent(
    placeholderState: PlaceholderConfig,
    transformer: ImageTransformer,
): InlineTextContent {

    val (width, height) = placeholderState.size.width.sp to placeholderState.size.height.sp
    val imageHandler = LocalImageLinkHandler.current
    // 使用 Map<String, MutableState<Rect>> 保存每个图片的坐标 State
    // 以 link 为 key，确保每个图片独立管理自己的坐标
    val frameStates = remember { mutableMapOf<String, MutableState<Rect>>() }
    val density = LocalDensity.current

    return InlineTextContent(
        Placeholder(width = width.value.sp, height = height.value.sp)
    ) { link ->
        transformer.transform(link, placeholderState.size, true)?.let { imageData ->

            imageHandler.store(imageData)
            // 为每个图片创建独立的 State
            val frameState = frameStates.getOrPut(link) { mutableStateOf(Rect.Zero) }

            QnImage(
                modifier = Modifier
                    .clickable {
                        imageHandler.preview(imageData, link, frameState.value, density)
                    }
                    .then(imageData.modifier)
                    .onGloballyPositioned {
                        frameState.value = it.boundsInRoot()
                    },
                painter = imageData.painter,
                contentDescription = imageData.contentDescription,
                alignment = imageData.alignment,
                contentScale = imageData.contentScale,
                alpha = imageData.alpha,
                colorFilter = imageData.colorFilter
            )
        }
    }
}

