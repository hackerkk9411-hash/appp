package com.example.iptvplayer.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.iptvplayer.R
import com.example.iptvplayer.model.Channel
import com.example.iptvplayer.parser.JsonPlaylistParser
import com.example.iptvplayer.parser.M3uParser
import com.example.iptvplayer.player.PlayerActivity
import com.example.iptvplayer.repository.PlaylistFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

@UnstableApi
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var channels: List<Channel> = emptyList()

    companion object {
        const val EXTRA_PLAYLIST_BODY = "extra_playlist_body"
        const val EXTRA_PLAYLIST_URL = "extra_playlist_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.channelRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val body = intent.getStringExtra(EXTRA_PLAYLIST_BODY)
        if (body != null) {
            // Came from the token-gated server response — could be raw M3U,
            // or a JSON [{name,url,logo}] bundle whose entries each point
            // to their own M3U that we need to fetch next.
            handleServerResponse(body)
        } else {
            // Opened directly (no token flow) — fall back to the bundled
            // sample playlist so the app is still runnable/testable.
            val parsed = resources.openRawResource(R.raw.sample_playlist).use { stream ->
                M3uParser.parse(stream)
            }
            loadChannels(parsed)
        }
    }

    private fun handleServerResponse(body: String) {
        val trimmed = body.trim()
        if (trimmed.startsWith("#EXTM3U")) {
            loadChannels(M3uParser.parse(ByteArrayInputStream(trimmed.toByteArray())))
            return
        }
        if (trimmed.startsWith("[")) {
            // JSON bundle: fetch each nested playlist and merge all channels.
            lifecycleScope.launch {
                val sources = runCatching { JsonPlaylistParser.parse(trimmed) }.getOrElse {
                    Toast.makeText(this@MainActivity, "Couldn't parse playlist bundle", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val merged = mutableListOf<Channel>()
                for (source in sources) {
                    val text = withContext(Dispatchers.IO) {
                        runCatching { PlaylistFetcher.fetchText(source.url) }.getOrNull()
                    } ?: continue
                    if (text.trim().startsWith("#EXTM3U")) {
                        merged += M3uParser.parse(ByteArrayInputStream(text.toByteArray()))
                    }
                }
                loadChannels(merged)
            }
            return
        }
        Toast.makeText(this, "Unrecognized playlist format from server", Toast.LENGTH_SHORT).show()
    }

    private fun loadChannels(list: List<Channel>) {
        channels = list
        recyclerView.adapter = ChannelAdapter(channels) { channel ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtras(PlayerActivity.channelToIntentExtras(channel))
            startActivity(intent)
        }
    }
}
