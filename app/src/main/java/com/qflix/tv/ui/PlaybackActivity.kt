package com.qflix.tv.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.qflix.tv.R

class PlaybackActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)

        if (savedInstanceState == null) {
            val f = PlaybackFragment()
            f.arguments = intent.extras
            supportFragmentManager.beginTransaction()
                .replace(R.id.playback_container, f)
                .commitNow()
        }
    }
}
