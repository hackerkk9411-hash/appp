package com.example.iptvplayer.tv

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.media3.common.util.UnstableApi
import com.example.iptvplayer.R
import com.example.iptvplayer.model.Channel
import com.example.iptvplayer.parser.M3uParser
import com.example.iptvplayer.player.PlayerActivity

@UnstableApi
class ChannelBrowseFragment : BrowseSupportFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Channel) {
                val intent = Intent(requireContext(), PlayerActivity::class.java)
                intent.putExtras(PlayerActivity.channelToIntentExtras(item))
                startActivity(intent)
            }
        }

        loadRows()
    }

    private fun loadRows() {
        val channels = resources.openRawResource(R.raw.sample_playlist).use { stream ->
            M3uParser.parse(stream)
        }

        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        channels.groupBy { it.group ?: "Channels" }.forEach { (groupName, groupChannels) ->
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            groupChannels.forEach { listRowAdapter.add(it) }
            val header = HeaderItem(groupName)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }

        adapter = rowsAdapter
    }
}
