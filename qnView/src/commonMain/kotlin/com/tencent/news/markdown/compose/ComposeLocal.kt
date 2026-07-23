package com.tencent.news.markdown.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.tencent.news.markdown.compose.components.MarkdownComponents
import com.tencent.news.markdown.compose.components.markdownComponents
import com.tencent.news.markdown.model.BulletHandler
import com.tencent.news.markdown.model.DefaultMarkdownAnnotator
import com.tencent.news.markdown.model.DefaultMarkdownAnnotatorConfig
import com.tencent.news.markdown.model.DefaultMarkdownExtendedSpans
import com.tencent.news.markdown.model.ImageLinkHandler
import com.tencent.news.markdown.model.ImageLinkHandlerImpl
import com.tencent.news.markdown.model.ImageTransformer
import com.tencent.news.markdown.model.MarkdownAnimations
import com.tencent.news.markdown.model.MarkdownAnnotator
import com.tencent.news.markdown.model.MarkdownColors
import com.tencent.news.markdown.model.MarkdownDimens
import com.tencent.news.markdown.model.MarkdownExtendedSpans
import com.tencent.news.markdown.model.MarkdownPadding
import com.tencent.news.markdown.model.MarkdownTypography
import com.tencent.news.markdown.model.NoOpTextProvider
import com.tencent.news.markdown.model.ReferenceLinkHandler
import com.tencent.news.markdown.model.TextProvider

/**
 * The CompositionLocal to provide functionality related to transforming the bullet of an ordered list
 */
internal val LocalBulletListHandler = staticCompositionLocalOf {
    return@staticCompositionLocalOf BulletHandler { _, _, _, _ -> "● " }
}

/**
 * The CompositionLocal to provide functionality related to transforming the bullet of an ordered list
 */
internal val LocalOrderedListHandler = staticCompositionLocalOf {
    return@staticCompositionLocalOf BulletHandler { _, _, index, _ -> "${index + 1}. " }
}

/**
 * Local [ReferenceLinkHandler] provider
 */
internal val LocalReferenceLinkHandler = staticCompositionLocalOf<ReferenceLinkHandler> {
    error("CompositionLocal ReferenceLinkHandler not present")
}

/**
 * Local [MarkdownColors] provider
 */
val LocalMarkdownColors = compositionLocalOf<MarkdownColors> {
    error("No local MarkdownColors")
}

/**
 * Local [MarkdownTypography] provider
 */
val LocalMarkdownTypography = compositionLocalOf<MarkdownTypography> {
    error("No local MarkdownTypography")
}

/**
 * Local [MarkdownPadding] provider
 */
val LocalMarkdownPadding = staticCompositionLocalOf<MarkdownPadding> {
    error("No local Padding")
}

/**
 * Local [MarkdownDimens] provider
 */
val LocalMarkdownDimens = compositionLocalOf<MarkdownDimens> {
    error("No local MarkdownDimens")
}

/**
 * Local [ImageTransformer] provider
 */
val LocalImageTransformer = staticCompositionLocalOf<ImageTransformer> {
    error("No local ImageTransformer")
}

val LocalImageLinkHandler = staticCompositionLocalOf<ImageLinkHandler> { ImageLinkHandlerImpl() }

val LocalTextProvider = staticCompositionLocalOf<TextProvider> { NoOpTextProvider() }

/**
 * Local [MarkdownAnnotator] provider
 */
val LocalMarkdownAnnotator = compositionLocalOf<MarkdownAnnotator> {
    return@compositionLocalOf DefaultMarkdownAnnotator(null, null, DefaultMarkdownAnnotatorConfig())
}

/**
 * Local [MarkdownExtendedSpans] provider
 */
val LocalMarkdownExtendedSpans = compositionLocalOf<MarkdownExtendedSpans> {
    return@compositionLocalOf DefaultMarkdownExtendedSpans(null)
}

/**
 * Local [MarkdownComponents] provider
 */
val LocalMarkdownComponents = compositionLocalOf<MarkdownComponents> {
    return@compositionLocalOf markdownComponents()
}

/**
 * Local [MarkdownAnimations] provider
 */
val LocalMarkdownAnimations = compositionLocalOf<MarkdownAnimations> {
    error("No local MarkdownAnimations")
}