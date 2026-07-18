package com.example.iptvplayer.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.iptvplayer.databinding.ActivityAddPlaylistBinding
import com.example.iptvplayer.model.PlaylistSource
import com.example.iptvplayer.repository.PlaylistRepository

/**
 * Lets the user add/remove their own playlist source URLs (M3U or the
 * JSON {name,url,logo} array format). Nothing is bundled or hardcoded here —
 * the user supplies whatever URL they own/are authorized to use.
 */
class AddPlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlaylistBinding
    private lateinit var repository: PlaylistRepository
    private lateinit var adapter: SourceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = PlaylistRepository(applicationContext)

        adapter = SourceAdapter(repository.getSources()) { source ->
            repository.removeSource(source.url)
            adapter.updateItems(repository.getSources())
        }
        binding.sourcesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.sourcesRecyclerView.adapter = adapter

        binding.addButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim().ifBlank { "My Playlist" }
            val url = binding.urlInput.text.toString().trim()
            if (url.isBlank()) {
                Toast.makeText(this, "Enter a playlist URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            repository.addSource(PlaylistSource(name = name, url = url))
            adapter.updateItems(repository.getSources())
            binding.nameInput.text?.clear()
            binding.urlInput.text?.clear()
            setResult(RESULT_OK)
        }
    }
}
