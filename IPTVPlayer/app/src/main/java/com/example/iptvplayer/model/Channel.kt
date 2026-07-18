package com.example.iptvplayer.model

/**
 * Represents one playable channel/entry parsed from an M3U playlist.
 *
 * Supports plain HLS/progressive streams as well as DASH streams protected
 * with ClearKey/Widevine DRM, and per-stream custom HTTP headers
 * (e.g. User-Agent, Cookie, Referer) which some IPTV providers require.
 */
data class Channel(
    val name: String,
    val logoUrl: String? = null,
    val group: String? = null,
    val streamUrl: String,
    // "dash", "hls" or "progressive" — auto-detected if null
    val streamType: String? = null,
    // DRM: "clearkey", "widevine", or null for no DRM
    val drmScheme: String? = null,
    // License server URL for DRM key retrieval
    val licenseUrl: String? = null,
    // Extra HTTP headers required to fetch the stream/license (User-Agent, Cookie, Referer, etc.)
    val headers: Map<String, String> = emptyMap()
)
