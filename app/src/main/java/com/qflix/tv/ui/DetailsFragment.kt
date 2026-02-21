package com.qflix.tv.ui

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.qflix.tv.R
import com.qflix.tv.model.MediaItemQ
import android.widget.Toast
import com.qflix.tv.data.UserLibraryStore
import com.qflix.tv.data.ContentStore
import com.qflix.tv.data.EpgRepository
import com.qflix.tv.data.EpgCache

class DetailsFragment : DetailsSupportFragment() {

    companion object {
        const val EXTRA_MEDIA = "extra_media"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val media = (arguments?.getSerializable(EXTRA_MEDIA) as? MediaItemQ)
            ?: error("Missing media")

        val presenterSelector = ClassPresenterSelector()
        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsOverviewPresenter())
        presenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)

        val a = ArrayObjectAdapter(presenterSelector)

        val row = DetailsOverviewRow(media).apply {
            val img = media.posterUrl
            if (!img.isNullOrBlank()) {
                Glide.with(requireContext())
                    .load(img)
                    .placeholder(R.drawable.placeholder_poster)
                    .error(R.drawable.placeholder_poster)
                    .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable,
                            transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                        ) {
                            setImageDrawable(resource)
                            a.notifyArrayItemRangeChanged(0, 1)
                        }
                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                    })
            }
            actionsAdapter = ArrayObjectAdapter().apply {
                add(Action(1, "Lire"))
                add(Action(2, "Favori"))
            }
            if (action.id == 2L) {
                // Toggle favorite
                val isFav = kotlinx.coroutines.runBlocking { library.toggleFavorite(media) }
                Toast.makeText(requireContext(), if (isFav) "Ajouté aux favoris" else "Retiré des favoris", Toast.LENGTH_SHORT).show()
            }
        }

        val library = UserLibraryStore(requireContext())
        val epgRepo = EpgRepository(EpgCache(requireContext()))

        detailsPresenter.setOnActionClickedListener { action ->
            if (action.id == 1L) {
                startActivity(Intent(requireContext(), PlaybackActivity::class.java).apply {
                    putExtra(EXTRA_MEDIA, media)
                })
            }
            if (action.id == 2L) {
                // Toggle favorite
                val isFav = kotlinx.coroutines.runBlocking { library.toggleFavorite(media) }
                Toast.makeText(requireContext(), if (isFav) "Ajouté aux favoris" else "Retiré des favoris", Toast.LENGTH_SHORT).show()
            }
        }

        a.add(row)
        adapter = a
    }

    private class DetailsOverviewPresenter : AbstractDetailsDescriptionPresenter() {
        override fun onBindDescription(vh: ViewHolder, item: Any) {
            val m = item as MediaItemQ
            vh.title.text = m.title
            vh.subtitle.text = m.category
            val base = m.overview ?: ""
if (m.isLive && ContentStore.epgPrograms.isNotEmpty()) {
    val key = m.tvgId ?: m.title
    val nn = epgRepo.nowNext(ContentStore.epgPrograms, key)
    val nowTxt = nn.now?.let { "• Maintenant: ${it.title}" } ?: ""
    val nextTxt = nn.next?.let { "• Ensuite: ${it.title}" } ?: ""
    val combined = listOf(base, nowTxt, nextTxt).filter { it.isNotBlank() }.joinToString("\n")
    vh.body.text = combined
} else {
    vh.body.text = base
}
        }
    }
}
