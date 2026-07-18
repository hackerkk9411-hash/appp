package com.example.iptvplayer.model

/**
 * A user-added playlist source: a name plus the URL to fetch (either a
 * plain M3U file, or a JSON array of {name, url, logo} bundles that each
 * point to their own M3U).
 */
data class PlaylistSource(
    val name: String,
    val url: String,
    val logo: String? = null
)
