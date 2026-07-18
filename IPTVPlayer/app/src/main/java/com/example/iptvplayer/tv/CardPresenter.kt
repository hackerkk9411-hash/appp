package com.example.iptvplayer.tv

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.example.iptvplayer.model.Channel

class CardPresenter : Presenter() {

    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

    class ViewHolder(val cardView: ImageCardView) : Presenter.ViewHolder(cardView)

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any) {
        val channel = item as Channel
        val holder = viewHolder as ViewHolder
        holder.cardView.titleText = channel.name
        holder.cardView.contentText = channel.group ?: channel.streamType?.uppercase() ?: ""
        holder.cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
        // Channel logos are loaded lazily; plug in Glide/Coil here if you want
        // artwork instead of the placeholder background.
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        (viewHolder as ViewHolder).cardView.mainImage = null
    }
}
