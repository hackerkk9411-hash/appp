package com.example.iptvplayer.parser

import com.example.iptvplayer.model.Channel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Parses extended M3U playlists.
 *
 * Understands the common IPTV-provider extensions on top of plain #EXTM3U:
 *  - #EXTINF:-1 tvg-id="" tvg-name="" tvg-logo="" group-title="",Channel Name
 *  - #KODIPROP:inputstream.adaptive.manifest_type=mpd
 *  - #KODIPROP:inputstream.adaptive.license_type=clearkey|widevine
 *  - #KODIPROP:inputstream.adaptive.license_key=<url or key>
 *  - #EXT-X-LICENSE-URL: <url>                      (alternate license tag some lists use)
 *  - Stream URL suffix "|Header1=Value1&Header2=Value2" for custom HTTP headers
 *    (User-Agent, Cookie, Referer, etc.) — a convention used by many IPTV apps.
 *
 * This parser is intentionally generic/provider-agnostic: it only understands
 * the *format*, and does not target or bundle any specific provider's content.
 */
object M3uParser {

    fun parse(input: InputStream): List<Channel> {
        val reader = BufferedReader(InputStreamReader(input))
        val channels = mutableListOf<Channel>()

        var name: String? = null
        var logo: String? = null
        var group: String? = null
        var manifestType: String? = null
        var licenseType: String? = null
        var licenseKey: String? = null

        reader.forEachLine { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    name = line.substringAfterLast(",", "").ifBlank { "Unnamed" }
                    logo = Regex("tvg-logo=\"([^\"]*)\"").find(line)?.groupValues?.get(1)
                    group = Regex("group-title=\"([^\"]*)\"").find(line)?.groupValues?.get(1)
                }
                line.startsWith("#KODIPROP:inputstream.adaptive.manifest_type=") -> {
                    manifestType = line.substringAfter("=").trim()
                }
                line.startsWith("#KODIPROP:inputstream.adaptive.license_type=") -> {
                    licenseType = line.substringAfter("=").trim()
                }
                line.startsWith("#KODIPROP:inputstream.adaptive.license_key=") -> {
                    licenseKey = line.substringAfter("=").trim()
                }
                line.startsWith("#EXT-X-LICENSE-URL") -> {
                    if (licenseKey == null) {
                        licenseKey = line.substringAfter(":").trim()
                    }
                }
                line.isNotBlank() && !line.startsWith("#") -> {
                    // This is the stream URL line, optionally with "|Header=Value&Header2=Value2"
                    val (url, headers) = splitUrlAndHeaders(line)
                    val type = manifestType?.let {
                        when (it.lowercase()) {
                            "mpd" -> "dash"
                            else -> it
                        }
                    } ?: guessTypeFromUrl(url)

                    channels.add(
                        Channel(
                            name = name ?: "Unnamed",
                            logoUrl = logo,
                            group = group,
                            streamUrl = url,
                            streamType = type,
                            drmScheme = licenseType,
                            licenseUrl = licenseKey,
                            headers = headers
                        )
                    )

                    // reset per-entry state
                    name = null; logo = null; group = null
                    manifestType = null; licenseType = null; licenseKey = null
                }
            }
        }
        return channels
    }

    private fun splitUrlAndHeaders(line: String): Pair<String, Map<String, String>> {
        val pipeIndex = line.indexOf('|')
        if (pipeIndex == -1) return line to emptyMap()

        val url = line.substring(0, pipeIndex)
        val paramsPart = line.substring(pipeIndex + 1)
        val headers = paramsPart.split("&")
            .mapNotNull {
                val idx = it.indexOf('=')
                if (idx == -1) null else it.substring(0, idx).trim() to it.substring(idx + 1).trim()
            }
            .toMap()
        return url to headers
    }

    private fun guessTypeFromUrl(url: String): String = when {
        url.contains(".mpd", ignoreCase = true) -> "dash"
        url.contains(".m3u8", ignoreCase = true) -> "hls"
        else -> "progressive"
    }
}
