package com.example.iptvplayer.parser

import com.example.iptvplayer.model.PlaylistSource
import org.json.JSONArray

/**
 * Parses the simple JSON array format some playlist sources use:
 * [{"name": "...", "url": "...", "logo": "..."}, ...]
 *
 * Each entry's "url" is expected to point to an actual M3U playlist,
 * which should then be fetched and passed to M3uParser separately.
 */
object JsonPlaylistParser {

    fun parse(jsonText: String): List<PlaylistSource> {
        val array = JSONArray(jsonText)
        val result = mutableListOf<PlaylistSource>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val name = obj.optString("name", "Unnamed")
            val url = obj.optString("url", "")
            val logo = obj.optString("logo", null)
            if (url.isNotBlank()) {
                result.add(PlaylistSource(name = name, url = url, logo = logo))
            }
        }
        return result
    }
}
