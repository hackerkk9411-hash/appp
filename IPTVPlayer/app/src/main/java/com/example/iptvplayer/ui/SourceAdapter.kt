package com.example.iptvplayer.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iptvplayer.R
import com.example.iptvplayer.model.PlaylistSource

class SourceAdapter(
    private var items: List<PlaylistSource>,
    private val onRemove: (PlaylistSource) -> Unit
) : RecyclerView.Adapter<SourceAdapter.SourceViewHolder>() {

    class SourceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.sourceName)
        val url: TextView = view.findViewById(R.id.sourceUrl)
        val removeButton: Button = view.findViewById(R.id.removeButton)
    }

    fun updateItems(newItems: List<PlaylistSource>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist_source, parent, false)
        return SourceViewHolder(view)
    }

    override fun onBindViewHolder(holder: SourceViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.url.text = item.url
        holder.removeButton.setOnClickListener { onRemove(item) }
    }

    override fun getItemCount(): Int = items.size
}
