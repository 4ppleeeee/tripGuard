package com.tencent.news.markdown.model

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementType
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.parser.MarkdownParser

internal object MarkdownCustomElementTypes {
    val VIDEO: IElementType = MarkdownElementType("VIDEO")
}

private val VIDEO_PLACEHOLDER_LINE_REGEX =
    Regex("(?m)^[ \\t]*\\[\\]\\(@video=(\\d+)\\)[ \\t]*$")
private val VIDEO_PLACEHOLDER_REGEX =
    Regex("^\\s*\\[\\]\\(@video=(\\d+)\\)\\s*$")

internal fun MarkdownParser.buildMarkdownNodesWithVideoPlaceholders(content: String): List<ASTNode> {
    val placeholders = VIDEO_PLACEHOLDER_LINE_REGEX.findAll(content).toList()
    if (placeholders.isEmpty()) {
        return buildMarkdownTreeFromString(content).children
    }

    val nodes = mutableListOf<ASTNode>()
    var cursor = 0

    placeholders.forEach { match ->
        val placeholderStart = match.range.first
        val placeholderEnd = match.range.last + 1

        if (cursor < placeholderStart) {
            nodes += parseSegmentAndShift(content.substring(cursor, placeholderStart), cursor)
        }

        nodes += LeafASTNode(
            type = MarkdownCustomElementTypes.VIDEO,
            startOffset = placeholderStart,
            endOffset = placeholderEnd
        )

        cursor = placeholderEnd
        if (cursor < content.length && content[cursor] == '\n') {
            cursor += 1
        }
    }

    if (cursor < content.length) {
        nodes += parseSegmentAndShift(content.substring(cursor), cursor)
    }

    return nodes
}

internal fun ASTNode.findVideoPlaceholderIndexOrNull(content: String): Int? {
    if (type != MarkdownCustomElementTypes.VIDEO) return null
    if (startOffset < 0 || endOffset > content.length || startOffset >= endOffset) return null
    val raw = content.substring(startOffset, endOffset)
    return VIDEO_PLACEHOLDER_REGEX.matchEntire(raw)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
}

private fun MarkdownParser.parseSegmentAndShift(segment: String, offset: Int): List<ASTNode> {
    if (segment.isBlank()) return emptyList()
    return buildMarkdownTreeFromString(segment)
        .children
        .map { it.shiftOffset(offset) }
}

private fun ASTNode.shiftOffset(offset: Int): ASTNode {
    if (offset == 0) return this
    return when (this) {
        is LeafASTNode -> LeafASTNode(type, startOffset + offset, endOffset + offset)
        is CompositeASTNode -> CompositeASTNode(type, children.map { it.shiftOffset(offset) })
        else -> if (children.isEmpty()) {
            LeafASTNode(type, startOffset + offset, endOffset + offset)
        } else {
            CompositeASTNode(type, children.map { it.shiftOffset(offset) })
        }
    }
}
