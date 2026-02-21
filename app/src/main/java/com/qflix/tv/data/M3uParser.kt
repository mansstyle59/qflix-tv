\
package com.qflix.tv.data

import com.qflix.tv.model.MediaItemQ
import java.util.UUID

object M3uParser {

    /**
     * Minimal M3U parser:
     * - Supports #EXTINF with tvg-name / group-title / tvg-logo (common conventions)
     * - Treats following line as media URL
     */
    fun parse(m3uText: String): List<MediaItemQ> {
        val lines = m3uText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val out = mutableListOf<MediaItemQ>()

        var currentTitle: String? = null
        var currentGroup: String? = null
        var currentLogo: String? = null
        var currentTvgId: String? = null
        var currentIsLive: Boolean = false

        for (line in lines) {
            if (line.startsWith("#EXTINF", ignoreCase = true)) {
                currentIsLive = line.contains("#EXTINF:-1")
                currentTitle = extractAttr(line, "tvg-name") ?: line.substringAfter(",").trim().ifBlank { null }
                currentGroup = extractAttr(line, "group-title") ?: "Catalogue"
                currentLogo = extractAttr(line, "tvg-logo")
                currentTvgId = extractAttr(line, "tvg-id")
            } else if (!line.startsWith("#")) {
                val url = line
                val title = currentTitle ?: "Titre"
                val group = currentGroup ?: "Catalogue"
                out += MediaItemQ(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    category = group,
                    videoUrl = url,
                    tvgId = currentTvgId,
                    posterUrl = currentLogo,
                    isLive = currentIsLive
                )
                currentTitle = null
                currentGroup = null
                currentLogo = null
                currentIsLive = false
                currentTvgId = null
            }
        }
        return out
    }

    private fun extractAttr(extinf: String, key: String): String? {
        val pattern = "\\b" + Regex.escape(key) + "\\s*=\\s*([\\\"'])(.*?)\\1"
        val r = Regex(pattern)
        return r.find(extinf)?.groupValues?.getOrNull(2)?.takeIf { it.isNotBlank() }
    }
}
