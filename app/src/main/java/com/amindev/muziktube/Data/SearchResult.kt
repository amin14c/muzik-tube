package com.amindev.muziktube.data

data class SearchResult(
    val videoId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val duration: Long // seconds
)
