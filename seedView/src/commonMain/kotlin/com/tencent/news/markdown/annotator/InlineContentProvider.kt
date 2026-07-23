package com.tencent.news.markdown.annotator

import com.tencent.kuikly.compose.foundation.text.InlineTextContent

internal interface InlineContentProvider {
    val inlineContent: Map<String, InlineTextContent>
}