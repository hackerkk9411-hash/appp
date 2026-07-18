package com.example.iptvplayer.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.iptvplayer.model.PlaylistSource
import org.json.JSONArray
import org.json.JSONObject

/**
 * Stores the list of playlist source URLs the *user* has added, encrypted
 * at rest (AES256-GCM via Jetpack Security) so the values aren't sitting in
 * a plaintext prefs XML file on disk. This protects against casual local
 * access (e.g. another app or a backup) — it is not a substitute for
 * server-side auth, and does not make a URL undiscoverable to someone who
 * controls the device and traffic to it.
 */
class PlaylistRepository(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "playlist_sources_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getSources(): List<PlaylistSource> {
        val raw = prefs.getString(KEY_SOURCES, null) ?: return emptyList()
        val array = JSONArray(raw)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            PlaylistSource(
                name = obj.getString("name"),
                url = obj.getString("url"),
                logo = obj.optString("logo", null)
            )
        }
    }

    fun addSource(source: PlaylistSource) {
        val current = getSources().toMutableList()
        current.removeAll { it.url == source.url }
        current.add(source)
        saveAll(current)
    }

    fun removeSource(url: String) {
        saveAll(getSources().filterNot { it.url == url })
    }

    private fun saveAll(sources: List<PlaylistSource>) {
        val array = JSONArray()
        sources.forEach { s ->
            array.put(JSONObject().apply {
                put("name", s.name)
                put("url", s.url)
                put("logo", s.logo ?: "")
            })
        }
        prefs.edit().putString(KEY_SOURCES, array.toString()).apply()
    }

    companion object {
        private const val KEY_SOURCES = "sources"
    }
}
