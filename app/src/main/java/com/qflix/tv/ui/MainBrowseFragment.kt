package com.qflix.tv.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.qflix.tv.R
import com.qflix.tv.data.ContentRepository
import com.qflix.tv.data.MetadataCache
import com.qflix.tv.data.UserLibraryStore
import com.qflix.tv.data.ContentStore
import com.qflix.tv.data.EpgCache
import com.qflix.tv.data.EpgRepository
import com.qflix.tv.data.SourceStore
import com.qflix.tv.model.MediaItemQ
import com.qflix.tv.model.Source
import com.qflix.tv.ui.presenter.CardPresenter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainBrowseFragment : BrowseSupportFragment() {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private val cardPresenter = CardPresenter()
    private lateinit var store: SourceStore
    private val repo by lazy { ContentRepository(MetadataCache(requireContext())) }
    private lateinit var library: UserLibraryStore
    private val epgRepo by lazy { EpgRepository(EpgCache(requireContext())) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = getString(R.string.app_name)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = 0xFF000000.toInt()

        store = SourceStore(requireContext())
        library = UserLibraryStore(requireContext())

        
setOnSearchClickedListener {
    startActivity(Intent(requireContext(), SearchActivity::class.java))
}

setOnItemViewLongClickedListener { _,_,_,_->
    startActivity(Intent(requireContext(), TvGuideActivity::class.java))
    true
}

 {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
        }
        adapter = rowsAdapter

        setOnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is MediaItemQ -> {
                    startActivity(Intent(requireContext(), DetailsActivity::class.java).apply {
                        putExtra(DetailsFragment.EXTRA_MEDIA, item)
                    })
                }
                is ActionItem -> {
                    when (item.action) {
                        ActionItem.Action.ADD_SOURCE -> AddSourceDialog().show(parentFragmentManager, "add_source")
                        ActionItem.Action.CLEAR_SOURCES -> lifecycleScope.launch { store.clearSources() }
                        ActionItem.Action.INFO -> { /* no-op */ }
                    }
                }
            }
        }

        lifecycleScope.launch {
            store.sourcesFlow.collectLatest { sources ->
                loadAllSources(sources)
            }
        }
    }

    private suspend fun loadAllSources(sources: List<Source>) {
        rowsAdapter.clear()

        val settingsAdapter = ArrayObjectAdapter(ActionPresenter()).apply {
            add(ActionItem(ActionItem.Action.ADD_SOURCE, getString(R.string.add_source)))
            add(ActionItem(ActionItem.Action.CLEAR_SOURCES, "Effacer les sources"))
        }
        rowsAdapter.add(ListRow(HeaderItem(0, getString(R.string.settings)), settingsAdapter))

        if (sources.isEmpty()) {
            val emptyAdapter = ArrayObjectAdapter(ActionPresenter()).apply {
                add(ActionItem(ActionItem.Action.ADD_SOURCE, "Ajoute une playlist M3U légitime"))
                add(ActionItem(ActionItem.Action.INFO, getString(R.string.tmdb_hint)))
            }
            rowsAdapter.add(ListRow(HeaderItem(1, "Démarrage"), emptyAdapter))
            return
        }

        // Load EPG (XMLTV) from first source that provides an EPG URL (optionnel)
val epgUrl = sources.firstOrNull { !it.epgUrl.isNullOrBlank() }?.epgUrl
if (!epgUrl.isNullOrBlank()) {
    val programs = epgRepo.loadXmlTv(epgUrl)
    if (programs.isNotEmpty()) {
        ContentStore.epgPrograms = programs
    }
}

val allItems = mutableListOf<MediaItemQ>()
        for (s in sources) {
            val items = repo.loadFromUrl(s.url)
            allItems += items.map { it.copy(category = if (it.category.isBlank()) s.name else it.category) }
        }

        
        // ===== TV EN DIRECT =====
        val liveItems = allItems.filter { it.isLive }
        if (liveItems.isNotEmpty()) {
            val liveAdapter = ArrayObjectAdapter(cardPresenter).apply {
                liveItems.take(200).forEach { add(it) }
            }
            rowsAdapter.add(ListRow(HeaderItem(4, "TV en direct"), liveAdapter))
        }
    
        // Keep in-memory list for Search
ContentStore.lastLoadedItems = allItems

// Reprendre (Recents)
val recents = library.recentsFlow.let { kotlinx.coroutines.flow.first(it) }
if (recents.isNotEmpty()) {
    val aRecents = ArrayObjectAdapter(cardPresenter).apply {
        recents.take(20).forEach { add(it.toMediaItem()) }
    }
    rowsAdapter.add(ListRow(HeaderItem(2, "Reprendre"), aRecents))
}

// Favoris
val favs = library.favoritesFlow.let { kotlinx.coroutines.flow.first(it) }
if (favs.isNotEmpty()) {
    val aFavs = ArrayObjectAdapter(cardPresenter).apply {
        favs.take(50).forEach { add(it) }
    }
    rowsAdapter.add(ListRow(HeaderItem(3, "Favoris"), aFavs))
}

val grouped = allItems.groupBy { it.category.ifBlank { "Catalogue" } }
        var headerId = 10L
        for ((cat, items) in grouped) {
            val a = ArrayObjectAdapter(cardPresenter).apply {
                items.take(120).forEach { add(it) }
            }
            rowsAdapter.add(ListRow(HeaderItem(headerId++, cat), a))
        }
    }

    fun addSource(source: Source) {
        lifecycleScope.launch { store.addSource(source) }
    }

    data class ActionItem(val action: Action, val title: String) {
        enum class Action { ADD_SOURCE, CLEAR_SOURCES, INFO }
    }

    class ActionPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return ViewHolder(v)
        }
        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
            val ai = item as ActionItem
            val tv = viewHolder.view.findViewById<TextView>(android.R.id.text1)
            tv.text = ai.title
            tv.setTextColor(0xFFFFFFFF.toInt())
        }
        override fun onUnbindViewHolder(viewHolder: ViewHolder) = Unit
    }
}
