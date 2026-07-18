package com.example.iptvplayer.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Fetches playlist text (M3U or JSON) from a remote URL. Runs on a background thread. */
object PlaylistFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Throws(IOException::class)
    fun fetchText(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Failed to fetch $url: HTTP ${response.code}")
            }
            return response.body?.string() ?: throw IOException("Empty response from $url")
        }
    }
}
