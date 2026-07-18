package com.example.iptvplayer.config

/**
 * Set this to the base URL of a server YOU control and are authorized to
 * distribute content from. The app will call:
 *   BASE_URL + "?token=" + <what the user typed on the token screen>
 * and expects back either an M3U playlist or the JSON [{name,url,logo}]
 * bundle format, only when the token is valid.
 *
 * This is intentionally left blank in the shipped project — fill in your
 * own endpoint before building.
 */
object AppConfig {
    const val PLAYLIST_SERVER_BASE_URL = "" // e.g. "https://your-own-worker.example.workers.dev/"
}
