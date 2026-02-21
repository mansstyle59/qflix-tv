package com.qflix.tv.data

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object XmlTvParser {

    // XMLTV times look like: 20240101183000 +0000
    private val fmt = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun parsePrograms(stream: InputStream, maxPrograms: Int = 20000): List<EpgProgram> {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(stream, null)

        val out = ArrayList<EpgProgram>(minOf(2000, maxPrograms))

        var event = parser.eventType
        var curChannel: String? = null
        var curStart: Long = 0L
        var curStop: Long = 0L
        var curTitle: String? = null
        var curDesc: String? = null
        var insideProgramme = false
        var curTag: String? = null

        while (event != XmlPullParser.END_DOCUMENT && out.size < maxPrograms) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    curTag = parser.name
                    if (parser.name == "programme") {
                        insideProgramme = true
                        curChannel = parser.getAttributeValue(null, "channel")
                        val start = parser.getAttributeValue(null, "start")
                        val stop = parser.getAttributeValue(null, "stop")
                        curStart = parseTime(start)
                        curStop = parseTime(stop)
                        curTitle = null
                        curDesc = null
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideProgramme) {
                        val t = parser.text?.trim().orEmpty()
                        if (t.isNotBlank()) {
                            when (curTag) {
                                "title" -> if (curTitle == null) curTitle = t
                                "desc" -> if (curDesc == null) curDesc = t
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "programme") {
                        insideProgramme = false
                        val ch = curChannel
                        val title = curTitle
                        if (!ch.isNullOrBlank() && !title.isNullOrBlank() && curStart > 0 && curStop > 0) {
                            out.add(EpgProgram(ch, title, curStart, curStop, curDesc))
                        }
                        curChannel = null
                        curTitle = null
                        curDesc = null
                    }
                    curTag = null
                }
            }
            event = parser.next()
        }
        return out
    }

    private fun parseTime(raw: String?): Long {
        if (raw.isNullOrBlank()) return 0L
        // some XMLTV omit timezone; assume +0000
        val s = raw.trim().let { if (it.length >= 19 && (it.contains("+") || it.contains("-"))) it else "$it +0000" }
        return runCatching { fmt.parse(s)?.time ?: 0L }.getOrDefault(0L)
    }
}
