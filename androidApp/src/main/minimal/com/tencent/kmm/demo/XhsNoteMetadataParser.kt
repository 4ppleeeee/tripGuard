package com.tencent.kmm.demo

import org.json.JSONObject

internal data class XhsNoteMetadata(
    val title: String?,
    val description: String?,
    val imageUrl: String?,
)

internal object XhsNoteMetadataParser {

    fun parse(url: String, html: String): XhsNoteMetadata? {
        val state = extractInitialState(html) ?: return null
        val root = runCatching { JSONObject(state) }.getOrNull() ?: return null
        val note = findNote(root, XhsShareParser.extractNoteId(url)) ?: return null
        return XhsNoteMetadata(
            title = note.optText("title"),
            description = note.optText("desc") ?: extractDomDescription(html),
            imageUrl = note.optJSONArray("imageList")
                ?.optJSONObject(0)
                ?.let { it.optText("urlDefault") ?: it.optText("url") },
        ).takeIf { !it.title.isNullOrBlank() || !it.description.isNullOrBlank() || !it.imageUrl.isNullOrBlank() }
    }

    private fun extractInitialState(html: String): String? {
        val marker = "window.__INITIAL_STATE__="
        val start = html.indexOf(marker)
        if (start < 0) return null
        val valueStart = start + marker.length
        val scriptEnd = html.indexOf("</script>", valueStart).let { if (it < 0) html.length else it }
        return html.substring(valueStart, scriptEnd)
            .trim()
            .trimEnd(';')
            .takeIf { it.startsWith("{") }
    }

    private fun findNote(root: JSONObject, noteId: String?): JSONObject? {
        val phoneNote = root.optJSONObject("noteData")
            ?.optJSONObject("data")
            ?.optJSONObject("noteData")
        if (phoneNote != null) {
            return phoneNote
        }

        val detailMap = root.optJSONObject("note")
            ?.optJSONObject("noteDetailMap")
            ?: return null
        if (!noteId.isNullOrBlank()) {
            detailMap.optJSONObject(noteId)?.optJSONObject("note")?.let { return it }
        }
        val keys = detailMap.keys()
        while (keys.hasNext()) {
            detailMap.optJSONObject(keys.next())?.optJSONObject("note")?.let { return it }
        }
        return null
    }

    private fun JSONObject.optText(name: String): String? =
        if (has(name) && !isNull(name)) optString(name).takeIf { it.isNotBlank() } else null

    private fun extractDomDescription(html: String): String? {
        val descBlock = Regex(
            "<div[^>]+class=[\"'][^\"']*author-desc-content[^\"']*[\"'][^>]*>(.*?)</div>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
        ).find(html)?.groupValues?.getOrNull(1) ?: return null
        return descBlock
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<!--.*?-->", setOf(RegexOption.DOT_MATCHES_ALL)), "")
            .replace(Regex("<[^>]+>"), "")
            .let(::htmlDecode)
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .takeIf { it.isNotBlank() }
    }

    private fun htmlDecode(value: String): String = value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .replace(Regex("&#(\\d+);")) { match ->
            match.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: match.value
        }
}
