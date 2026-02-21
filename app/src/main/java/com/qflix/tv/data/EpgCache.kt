package com.qflix.tv.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "qflix_epg_cache")

class EpgCache(private val context: Context) {

    private val KEY = stringPreferencesKey("xmltv_programs_json")

    suspend fun get(maxAgeMs: Long): List<EpgProgram>? {
        val prefs = context.dataStore.data.first()
        val raw = prefs[KEY].orEmpty()
        if (raw.isBlank()) return null
        return runCatching {
            val root = JSONObject(raw)
            val cachedAt = root.optLong("cachedAt", 0L)
            if (cachedAt <= 0L) return null
            if (System.currentTimeMillis() - cachedAt > maxAgeMs) return null
            val arr = root.optJSONArray("programs") ?: return null
            val out = ArrayList<EpgProgram>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    EpgProgram(
                        channelId = o.optString("channelId"),
                        title = o.optString("title"),
                        startMs = o.optLong("startMs"),
                        endMs = o.optLong("endMs"),
                        desc = o.optString("desc").takeIf { it.isNotBlank() }
                    )
                )
            }
            out
        }.getOrNull()
    }

    suspend fun put(programs: List<EpgProgram>) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray()
            for (p in programs.take(20000)) {
                arr.put(JSONObject().apply {
                    put("channelId", p.channelId)
                    put("title", p.title)
                    put("startMs", p.startMs)
                    put("endMs", p.endMs)
                    put("desc", p.desc ?: "")
                })
            }
            val root = JSONObject().apply {
                put("cachedAt", System.currentTimeMillis())
                put("programs", arr)
            }
            prefs[KEY] = root.toString()
        }
    }
}
