package com.qflix.tv.model

import java.io.Serializable

data class MediaItemQ(
    val id: String,
    val title: String,
    val category: String,
    val tvgId: String? = null,
    val videoUrl: String,
    val posterUrl: String? = null,
    val overview: String? = null,
    val isLive: Boolean = false
) : Serializable
