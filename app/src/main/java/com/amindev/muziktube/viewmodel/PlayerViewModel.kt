package com.amindev.muziktube.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.amindev.muziktube.data.*
import com.amindev.muziktube.service.MusicService
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlayerState(
    val currentSong : SearchResult? = null,
    val isPlaying   : Boolean = false,
    val isLoading   : Boolean = false,
    val error       : String? = null
)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MusicRepository(AppDatabase.get(app).songDao())

    private val _search  = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _search.asStateFlow()

    private val _state   = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _state.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _loading.asStateFlow()

    val favorites = repo.getFavorites()

    private var controller: MediaController? = null

    // ── Player listener ──────────────────────────────────────────────────
    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
        }
        override fun onPlaybackStateChanged(state: Int) {
            _state.update { it.copy(isLoading = state == Player.STATE_BUFFERING) }
        }
    }

    init {
        val token = SessionToken(app, ComponentName(app, MusicService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            runCatching {
                controller = future.get().also { it.addListener(listener) }
            }
        }, MoreExecutors.directExecutor())
    }

    // ── Search ───────────────────────────────────────────────────────────
    fun search(query: String) = viewModelScope.launch {
        _loading.value = true
        _search.value  = emptyList()
        _search.value  = repo.search(query)
        _loading.value = false
    }

    // ── Playback ─────────────────────────────────────────────────────────
    fun playSong(song: SearchResult) = viewModelScope.launch {
        _state.update { it.copy(currentSong = song, isLoading = true, error = null) }

        // Start service
        getApplication<Application>().also { ctx ->
            ctx.startForegroundService(Intent(ctx, MusicService::class.java))
        }

        val url = repo.getStreamUrl(song.videoId)
        if (url != null) {
            controller?.run {
                setMediaItem(MediaItem.fromUri(url))
                prepare()
                play()
            }
            _state.update { it.copy(isLoading = false) }
        } else {
            _state.update { it.copy(isLoading = false, error = "فشل تحميل الأغنية ❌") }
        }
    }

    fun togglePlayPause() {
        controller?.let { c ->
            if (c.isPlaying) c.pause() else c.play()
        }
    }

    // ── Favorites ─────────────────────────────────────────────────────────
    fun addFavorite(song: SearchResult) = viewModelScope.launch {
        repo.addFavorite(Song(song.videoId, song.title, song.artist, song.thumbnailUrl, song.duration))
    }

    fun removeFavorite(song: Song) = viewModelScope.launch {
        repo.removeFavorite(song)
    }

    override fun onCleared() {
        controller?.release()
        super.onCleared()
    }
}
