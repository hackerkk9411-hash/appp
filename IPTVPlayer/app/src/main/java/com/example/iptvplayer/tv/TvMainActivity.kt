package com.example.iptvplayer.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.example.iptvplayer.R

@UnstableApi
class TvMainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tvBrowseFragmentContainer, ChannelBrowseFragment())
                .commitNow()
        }
    }
}
