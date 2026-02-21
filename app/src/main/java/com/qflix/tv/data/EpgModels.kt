package com.qflix.tv.data

data class EpgProgram(
    val channelId: String,
    val title: String,
    val startMs: Long,
    val endMs: Long,
    val desc: String?
)

data class NowNext(
    val now: EpgProgram?,
    val next: EpgProgram?
)
