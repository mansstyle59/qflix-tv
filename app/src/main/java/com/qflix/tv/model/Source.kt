package com.qflix.tv.model

data class Source(
    val name: String,
    val url: String,
    val epgUrl: String? = null
)
