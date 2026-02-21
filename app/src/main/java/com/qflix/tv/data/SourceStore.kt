package com.qflix.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.qflix.tv.model.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "qflix_prefs")

class SourceStore(private val context: Context) {

    private val KEY_SOURCES = stringPreferencesKey("sources_json")

    val sourcesFlow: Flow<List<Source>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[KEY_SOURCES].orEmpty()
            if (raw.isBlank()) return@map emptyList()
            runCatching {
                val arr = JSONArray(raw)
                (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    Source(
                        name = o.optString("name"),
                        url = o.optString("url"),
                        epgUrl = o.optString("epgUrl").takeIf { it.isNotBlank() }
                    )
                }.filter { it.name.isNotBlank() && it.url.isNotBlank() }
            }.getOrDefault(emptyList())
        }

    suspend fun addSource(source: Source) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SOURCES].orEmpty()
            val arr = if (current.isBlank()) JSONArray() else JSONArray(current)
            val obj = JSONObject().apply {
                put("name", source.name)
                put("url", source.url)
                put("epgUrl", source.epgUrl ?: "")
            }
            arr.put(obj)
            prefs[KEY_SOURCES] = arr.toString()
        }
    }

    suspend fun clearSources() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_SOURCES)
        }
    }
}
