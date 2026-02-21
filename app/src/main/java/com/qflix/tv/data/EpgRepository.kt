package com.qflix.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class EpgRepository(
    private val cache: EpgCache,
    private val http: OkHttpClient = OkHttpClient()
) {

    suspend fun loadXmlTv(epgUrl: String): List<EpgProgram> = withContext(Dispatchers.IO) {
        // 6h cache
        cache.get(6L * 60 * 60 * 1000)?.let { return@withContext it }

        val req = Request.Builder().url(epgUrl).get().build()
        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@withContext emptyList()
            val body = resp.body ?: return@withContext emptyList()
            val programs = runCatching { XmlTvParser.parsePrograms(body.byteStream()) }.getOrDefault(emptyList())
            if (programs.isNotEmpty()) cache.put(programs)
            programs
        }
    }

    fun nowNext(programs: List<EpgProgram>, channelKey: String): NowNext {
        val now = System.currentTimeMillis()
        val channelPrograms = programs.filter { it.channelId.equals(channelKey, ignoreCase = true) }
        val current = channelPrograms.firstOrNull { now in it.startMs..it.endMs }
        val next = channelPrograms
            .filter { it.startMs > now }
            .minByOrNull { it.startMs }
        return NowNext(current, next)
    }
}
