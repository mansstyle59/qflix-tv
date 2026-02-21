package com.qflix.tv.data

import com.qflix.tv.BuildConfig
import com.qflix.tv.model.MediaItemQ
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLConnection

class ContentRepository(private val cache: MetadataCache? = null) {

    private val http = OkHttpClient()

    suspend fun loadFromUrl(url: String): List<MediaItemQ> = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext emptyList()
            val body = resp.body?.string().orEmpty()
            val contentType = resp.header("Content-Type").orEmpty()

            val isM3u = body.contains("#EXTM3U", ignoreCase = true) ||
                contentType.contains("mpegurl", ignoreCase = true) ||
                contentType.contains("text", ignoreCase = true)

            if (isM3u) {
                val items = M3uParser.parse(body)
                if (BuildConfig.TMDB_API_KEY.isNotBlank()) enrichWithTmdb(items) else items
            } else {
                val hint = URLConnection.guessContentTypeFromName(url) ?: "Vidéo"
                listOf(
                    MediaItemQ(
                        id = url,
                        title = "Lecture",
                        category = "Liens",
                        videoUrl = url,
                        overview = hint
                    )
                )
            }
        }
    }

    private suspend fun enrichWithTmdb(items: List<MediaItemQ>): List<MediaItemQ> = withContext(Dispatchers.IO) {
        val key = BuildConfig.TMDB_API_KEY
        val out = mutableListOf<MediaItemQ>()
        for (it in items) {
            val titleKey = it.title.trim().lowercase()
            val cached = cache?.let { c -> runCatching { c.get(titleKey, 7L*24*60*60*1000) }.getOrNull() }
            val enriched = if (cached != null) TmdbHit(cached.posterUrl, cached.overview)
            else runCatching { tmdbSearchFirst(it.title, key) }.getOrNull()
            if (enriched != null) {
                if (cached == null) { cache?.let { c -> runCatching { c.put(titleKey, enriched.posterUrl, enriched.overview) }.getOrNull() } }
                out += it.copy(
                posterUrl = enriched.posterUrl ?: it.posterUrl,
                overview = enriched.overview ?: it.overview
            )
            } else out += it
        }
        out
    }

    private data class TmdbHit(val posterUrl: String?, val overview: String?)

    private fun tmdbSearchFirst(query: String, apiKey: String): TmdbHit? {
        val url = "https://api.themoviedb.org/3/search/multi?api_key=$apiKey&language=fr-FR&query=" +
            java.net.URLEncoder.encode(query, "UTF-8")
        val req = Request.Builder().url(url).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val body = resp.body?.string().orEmpty()
            val json = JSONObject(body)
            val results = json.optJSONArray("results") ?: return null
            if (results.length() == 0) return null
            val first = results.getJSONObject(0)
            val posterPath = first.optString("poster_path").takeIf { it.isNotBlank() }
            val overview = first.optString("overview").takeIf { it.isNotBlank() }
            val posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
            return TmdbHit(posterUrl, overview)
        }
    }
}
