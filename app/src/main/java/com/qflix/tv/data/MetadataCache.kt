package com.qflix.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "qflix_metadata_cache")

class MetadataCache(private val context: Context) {

    private val KEY_CACHE = stringPreferencesKey("tmdb_cache_json")

    data class Entry(
        val posterUrl: String?,
        val overview: String?,
        val cachedAt: Long
    )

    suspend fun get(titleKey: String, maxAgeMs: Long): Entry? {
        val prefs = context.dataStore.data.first()
        val raw = prefs[KEY_CACHE].orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            val root = JSONObject(raw)
            val o = root.optJSONObject(titleKey) ?: return null
            val cachedAt = o.optLong("cachedAt", 0L)
            if (cachedAt <= 0L) return null
            if (System.currentTimeMillis() - cachedAt > maxAgeMs) return null
            Entry(
                posterUrl = o.optString("posterUrl").takeIf { it.isNotBlank() },
                overview = o.optString("overview").takeIf { it.isNotBlank() },
                cachedAt = cachedAt
            )
        }.getOrNull()
    }

    suspend fun put(titleKey: String, posterUrl: String?, overview: String?) {
        context.dataStore.edit { prefs ->
            val raw = prefs[KEY_CACHE].orEmpty()
            val root = if (raw.isBlank()) JSONObject() else JSONObject(raw)
            val o = JSONObject().apply {
                put("posterUrl", posterUrl ?: "")
                put("overview", overview ?: "")
                put("cachedAt", System.currentTimeMillis())
            }
            root.put(titleKey, o)
            prefs[KEY_CACHE] = root.toString()
        }
    }
}
