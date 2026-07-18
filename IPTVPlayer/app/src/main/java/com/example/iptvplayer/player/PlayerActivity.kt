package com.example.iptvplayer.player

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.iptvplayer.model.Channel

/**
 * Full-screen playback activity. Builds an ExoPlayer MediaSource according to
 * the Channel's declared type (dash/hls/progressive), attaches ClearKey/Widevine
 * DRM configuration when present, and applies any custom HTTP headers
 * (User-Agent, Cookie, Referer, ...) required to fetch the manifest/segments/license.
 */
@UnstableApi
class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_DRM_SCHEME = "extra_drm_scheme"
        const val EXTRA_LICENSE_URL = "extra_license_url"
        const val EXTRA_HEADERS = "extra_headers" // HashMap<String, String>

        fun channelToIntentExtras(channel: Channel): Bundle = Bundle().apply {
            putString(EXTRA_NAME, channel.name)
            putString(EXTRA_URL, channel.streamUrl)
            putString(EXTRA_TYPE, channel.streamType)
            putString(EXTRA_DRM_SCHEME, channel.drmScheme)
            putString(EXTRA_LICENSE_URL, channel.licenseUrl)
            putSerializable(EXTRA_HEADERS, HashMap(channel.headers))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val playerView = PlayerView(this)
        setContentView(playerView)

        val url = intent.getStringExtra(EXTRA_URL) ?: return
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "progressive"
        val drmScheme = intent.getStringExtra(EXTRA_DRM_SCHEME)
        val licenseUrl = intent.getStringExtra(EXTRA_LICENSE_URL)
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        val headers = (intent.getSerializableExtra(EXTRA_HEADERS) as? HashMap<String, String>) ?: hashMapOf()

        val exoPlayer = ExoPlayer.Builder(this).build().also { player = it }
        playerView.player = exoPlayer

        val mediaSource = buildMediaSource(url, type, drmScheme, licenseUrl, headers)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun httpDataSourceFactory(headers: Map<String, String>): DataSource.Factory {
        val userAgent = headers["User-Agent"] ?: "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE})"
        return DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setDefaultRequestProperties(headers.filterKeys { it != "User-Agent" })
            .setAllowCrossProtocolRedirects(true)
    }

    private fun buildMediaSource(
        url: String,
        type: String,
        drmScheme: String?,
        licenseUrl: String?,
        headers: Map<String, String>
    ): MediaSource {
        val dataSourceFactory = httpDataSourceFactory(headers)

        val mediaItemBuilder = MediaItem.Builder().setUri(Uri.parse(url))

        // Attach DRM configuration if this channel declares one.
        if (drmScheme != null && licenseUrl != null) {
            val drmUuid = when (drmScheme.lowercase()) {
                "clearkey" -> androidx.media3.common.C.CLEARKEY_UUID
                "widevine" -> androidx.media3.common.C.WIDEVINE_UUID
                else -> null
            }
            if (drmUuid != null) {
                mediaItemBuilder.setDrmConfiguration(
                    MediaItem.DrmConfiguration.Builder(drmUuid)
                        .setLicenseUri(licenseUrl)
                        .setLicenseRequestHeaders(headers)
                        .build()
                )
            }
        }

        when (type.lowercase()) {
            "dash" -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_MPD)
            "hls" -> mediaItemBuilder.setMimeType(MimeTypes.APPLICATION_M3U8)
        }

        val mediaItem = mediaItemBuilder.build()
        val drmSessionManagerProvider = DefaultDrmSessionManagerProvider().apply {
            setDrmHttpDataSourceFactory(dataSourceFactory)
        }

        return when (type.lowercase()) {
            "dash" -> DashMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider(drmSessionManagerProvider)
                .createMediaSource(mediaItem)
            "hls" -> HlsMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider(drmSessionManagerProvider)
                .createMediaSource(mediaItem)
            else -> ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
    }
}
