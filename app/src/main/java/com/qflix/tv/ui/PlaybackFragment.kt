package com.qflix.tv.ui

import android.net.Uri
import android.os.Bundle
import androidx.leanback.app.PlaybackSupportFragment
import androidx.leanback.app.PlaybackSupportFragmentGlueHost
import androidx.leanback.media.PlaybackTransportControlGlue
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.leanback.LeanbackPlayerAdapter
import com.qflix.tv.model.MediaItemQ
import com.qflix.tv.data.UserLibraryStore
import kotlinx.coroutines.runBlocking
import android.view.KeyEvent
import android.widget.Toast
import com.qflix.tv.data.ContentStore

class PlaybackFragment : PlaybackSupportFragment() {

    private var player: ExoPlayer? = null
    private var glue: PlaybackTransportControlGlue<LeanbackPlayerAdapter>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val media = (arguments?.getSerializable(DetailsFragment.EXTRA_MEDIA) as? MediaItemQ)
            ?: error("Missing media")

        val library = UserLibraryStore(requireContext())

        val exo = ExoPlayer.Builder(requireContext()).build()
        player = exo

        val adapter = LeanbackPlayerAdapter(requireContext(), exo, 16)
        glue = PlaybackTransportControlGlue(requireContext(), adapter).apply {
            host = PlaybackSupportFragmentGlueHost(this@PlaybackFragment)
            title = media.title
            subtitle = media.category
            playWhenPrepared()
        }

        exo.setMediaItem(MediaItem.fromUri(Uri.parse(media.videoUrl)))
        exo.prepare()

        // Resume
        val resumePos = runBlocking {
            // best-effort: read recents and find position
            val recents = library.recentsFlow.let { kotlinx.coroutines.flow.first(it) }
            recents.firstOrNull { it.videoUrl == media.videoUrl }?.lastPositionMs ?: 0L
        }
        if (!media.isLive && resumePos > 5_000) {
            exo.seekTo(resumePos)
        }
// Zapping rapide pour TV en direct: DPAD LEFT/RIGHT
if (media.isLive) {
    val liveList = ContentStore.lastLoadedItems.filter { it.isLive }
    val currentIndex = liveList.indexOfFirst { it.videoUrl == media.videoUrl }.let { if (it < 0) 0 else it }
    var idx = currentIndex

    requireActivity().window.decorView.setOnKeyListener { _, keyCode, event ->
        if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
        if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT && keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) return@setOnKeyListener false
        if (liveList.isEmpty()) return@setOnKeyListener false

        idx = if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) (idx + 1) % liveList.size else (idx - 1 + liveList.size) % liveList.size
        val next = liveList[idx]
        Toast.makeText(requireContext(), "Chaîne: ${next.title}", Toast.LENGTH_SHORT).show()
        exo.setMediaItem(MediaItem.fromUri(Uri.parse(next.videoUrl)))
        exo.prepare()
        exo.play()
        true
    }
}

    }

    override fun onDestroy() {
        super.onDestroy()
        val pos = player?.currentPosition ?: 0L
        runBlocking { library.upsertRecent(media, pos) }
        glue?.pause()
        player?.release()
        player = null
    }
}
