package com.qflix.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qflix.tv.model.MediaItemQ
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "qflix_library")

/**
 * Stores:
 * - favorites: list of MediaItemQ
 * - recents: list of MediaItemQ + lastPositionMs + lastPlayedAt
 */
class UserLibraryStore(private val context: Context) {

    private val KEY_FAVORITES = stringPreferencesKey("favorites_json")
    private val KEY_RECENTS = stringPreferencesKey("recents_json")
    private val KEY_FAV_LIVE = stringPreferencesKey("favorite_live_urls")

    val favoritesFlow: Flow<List<MediaItemQ>> = context.dataStore.data.map { prefs ->
        parseMediaList(prefs[KEY_FAVORITES].orEmpty())
    }

    val recentsFlow: Flow<List<RecentEntry>> = context.dataStore.data.map { prefs ->
        parseRecentList(prefs[KEY_RECENTS].orEmpty())
    }

    suspend fun toggleFavorite(item: MediaItemQ): Boolean {
        // returns new isFavorite
        var isFav = false
        context.dataStore.edit { prefs ->
            val cur = parseMediaList(prefs[KEY_FAVORITES].orEmpty()).toMutableList()
            val idx = cur.indexOfFirst { it.videoUrl == item.videoUrl }
            if (idx >= 0) {
                cur.removeAt(idx)
                isFav = false
            } else {
                cur.add(0, item)
                isFav = true
            }
            prefs[KEY_FAVORITES] = mediaListToJson(cur)
        }
        return isFav
    }

    suspend fun isFavorite(videoUrl: String): Boolean {
        // best-effort (not reactive)
        // Prefer using favoritesFlow in UI, but this is handy for Details.
        return false
    }

    suspend fun upsertRecent(item: MediaItemQ, positionMs: Long) {
        context.dataStore.edit { prefs ->
            val cur = parseRecentList(prefs[KEY_RECENTS].orEmpty()).toMutableList()
            val now = System.currentTimeMillis()
            // remove existing
            cur.removeAll { it.videoUrl == item.videoUrl }
            cur.add(0, RecentEntry.from(item, positionMs, now))
            // cap
            val capped = cur.take(20)
            prefs[KEY_RECENTS] = recentListToJson(capped)
        }
    }

    suspend fun getResumePosition(videoUrl: String): Long {
        // Not reactive, used by player on start
        val prefs = context.dataStore.data.map { it }.let { flow -> kotlinx.coroutines.flow.first(flow) }
        val list = parseRecentList(prefs[KEY_RECENTS].orEmpty())
        return list.firstOrNull { it.videoUrl == videoUrl }?.lastPositionMs ?: 0L
    }

    data class RecentEntry(
        val id: String,
        val title: String,
        val category: String,
        val tvgId: String?,
        val videoUrl: String,
        val isLive: Boolean,
        val posterUrl: String?,
        val overview: String?,
        val lastPositionMs: Long,
        val lastPlayedAt: Long
    ) {
        fun toMediaItem(): MediaItemQ = MediaItemQ(
            id = id,
            title = title,
            category = category,
            tvgId = tvgId,
            videoUrl = videoUrl,
            isLive = isLive,
            posterUrl = posterUrl,
            overview = overview
        )

        companion object {
            fun from(item: MediaItemQ, pos: Long, at: Long) = RecentEntry(
                id = item.id,
                title = item.title,
                category = item.category,
                tvgId = item.tvgId,
                videoUrl = item.videoUrl,
                isLive = item.isLive,
                posterUrl = item.posterUrl,
                overview = item.overview,
                lastPositionMs = pos,
                lastPlayedAt = at
            )
        }
    }

    private fun parseMediaList(raw: String): List<MediaItemQ> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                MediaItemQ(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    category = o.optString("category"),
                    videoUrl = o.optString("videoUrl"),
                    posterUrl = o.optString("posterUrl").takeIf { it.isNotBlank() },
                    overview = o.optString("overview").takeIf { it.isNotBlank() },
                        isLive = o.optBoolean("isLive", false)
                )
            }.filter { it.videoUrl.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun mediaListToJson(list: List<MediaItemQ>): String {
        val arr = JSONArray()
        for (m in list) {
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("title", m.title)
                put("category", m.category)
                put("tvgId", m.tvgId ?: "")
                put("videoUrl", m.videoUrl)
                put("isLive", m.isLive)
                put("posterUrl", m.posterUrl ?: "")
                put("overview", m.overview ?: "")
            })
        }
        return arr.toString()
    }

    private fun parseRecentList(raw: String): List<RecentEntry> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                RecentEntry(
                    id = o.optString("id"),
                    title = o.optString("title"),
                    category = o.optString("category"),
                    tvgId = o.optString("tvgId").takeIf { it.isNotBlank() },
                    videoUrl = o.optString("videoUrl"),
                    isLive = o.optBoolean("isLive", false),
                    posterUrl = o.optString("posterUrl").takeIf { it.isNotBlank() },
                    overview = o.optString("overview").takeIf { it.isNotBlank() },
                    lastPositionMs = o.optLong("lastPositionMs", 0L),
                    lastPlayedAt = o.optLong("lastPlayedAt", 0L)
                )
            }.filter { it.videoUrl.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    private fun recentListToJson(list: List<RecentEntry>): String {
        val arr = JSONArray()
        for (r in list) {
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("title", r.title)
                put("category", r.category)
                put("tvgId", r.tvgId ?: "")
                put("videoUrl", r.videoUrl)
                put("isLive", r.isLive)
                put("posterUrl", r.posterUrl ?: "")
                put("overview", r.overview ?: "")
                put("lastPositionMs", r.lastPositionMs)
                put("lastPlayedAt", r.lastPlayedAt)
            })
        }
        return arr.toString()
    }
}


suspend fun toggleFavoriteLive(url: String) {
    context.dataStore.edit { prefs ->
        val raw = prefs[KEY_FAV_LIVE].orEmpty()
        val list = raw.split("|").filter { it.isNotBlank() }.toMutableSet()
        if (list.contains(url)) list.remove(url) else list.add(url)
        prefs[KEY_FAV_LIVE] = list.joinToString("|")
    }
}

suspend fun getFavoriteLive(): Set<String> {
    val prefs = context.dataStore.data.map { it }.let { kotlinx.coroutines.flow.first(it) }
    return prefs[KEY_FAV_LIVE].orEmpty().split("|").filter { it.isNotBlank() }.toSet()
}
