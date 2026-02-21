package com.qflix.tv.ui

import android.content.Intent
import android.os.Bundle
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.HeaderItem
import com.qflix.tv.data.ContentStore
import com.qflix.tv.model.MediaItemQ
import com.qflix.tv.ui.presenter.CardPresenter

class QflixSearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {

    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private val cardPresenter = CardPresenter()
    private val resultsAdapter = ArrayObjectAdapter(cardPresenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)

        setOnItemViewClickedListener { _, item, _, _ ->
            if (item is MediaItemQ) {
                startActivity(Intent(requireContext(), DetailsActivity::class.java).apply {
                    putExtra(DetailsFragment.EXTRA_MEDIA, item)
                })
            }
        }
    }

    override fun getResultsAdapter() = rowsAdapter

    override fun onQueryTextChange(newQuery: String?): Boolean {
        updateResults(newQuery.orEmpty())
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        updateResults(query.orEmpty())
        return true
    }

    private fun updateResults(q: String) {
        val query = q.trim()
        rowsAdapter.clear()
        resultsAdapter.clear()

        if (query.isBlank()) return

        val items = ContentStore.lastLoadedItems
        val hits = items.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            (it.overview?.contains(query, ignoreCase = true) == true)
        }.take(60)

        hits.forEach { resultsAdapter.add(it) }

        rowsAdapter.add(
            ListRow(
                HeaderItem(0, "Résultats"),
                resultsAdapter
            )
        )
    }
}
