package com.amindev.muziktube.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList.YouTube
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class MusicRepository(private val dao: SongDao) {

    suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        runCatching {
            val extractor = YouTube.getSearchExtractor(query)
            extractor.fetchPage()
            extractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .map { item ->
                    SearchResult(
                        videoId = item.url.substringAfter("watch?v=").substringBefore("&"),
                        title = item.name,
                        artist = item.uploaderName ?: "Unknown",
                        thumbnailUrl = item.thumbnails.firstOrNull()?.url ?: "",
                        duration = item.duration
                    )
                }
        }.getOrDefault(emptyList())
    }

    suspend fun getStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val extractor = YouTube.getStreamExtractor(url)
            extractor.fetchPage()

            val audioStreams = extractor.audioStreams
            if (audioStreams.isNullOrEmpty()) return@runCatching null

            audioStreams
                .filter { it.content != null && it.content.isNotEmpty() }
                .maxByOrNull { it.averageBitrate }
                ?.content
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    fun getFavorites() = dao.getAll()
    suspend fun addFavorite(song: Song) = dao.insert(song)
    suspend fun removeFavorite(song: Song) = dao.delete(song)
    suspend fun isFavorite(id: String) = dao.isFavorite(id)
}
