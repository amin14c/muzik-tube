package com.amindev.muziktube.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amindev.muziktube.data.SearchResult
import com.amindev.muziktube.data.Song
import com.amindev.muziktube.ui.components.PlayerBar
import com.amindev.muziktube.ui.theme.*
import com.amindev.muziktube.viewmodel.PlayerViewModel

@Composable
fun HomeScreen(vm: PlayerViewModel) {
    val results    by vm.searchResults.collectAsState()
    val state      by vm.playerState.collectAsState()
    val isSearching by vm.isSearching.collectAsState()
    val favorites  by vm.favorites.collectAsState(initial = emptyList())

    var query        by remember { mutableStateOf("") }
    var showFavs     by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
    ) {
        // ── Top Bar ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎵",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("ابحث عن أغنية...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = RedPrimary,
                    unfocusedBorderColor = SurfaceVariant,
                    focusedTextColor     = Color.White,
                    unfocusedTextColor   = Color.White,
                    cursorColor          = RedPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (query.isNotBlank()) {
                        showFavs = false
                        vm.search(query)
                        focusManager.clearFocus()
                    }
                })
            )
            IconButton(onClick = {
                showFavs = !showFavs
                focusManager.clearFocus()
            }) {
                Icon(
                    imageVector = if (showFavs) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorites",
                    tint = if (showFavs) RedPrimary else TextSecondary
                )
            }
        }

        // ── Content ──────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                isSearching -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = RedPrimary)
                    }
                }
                showFavs -> {
                    if (favorites.isEmpty()) {
                        EmptyState("لا توجد مفضلة بعد\nاضغط ❤️ على أي أغنية")
                    } else {
                        LazyColumn {
                            items(favorites, key = { it.videoId }) { song ->
                                SongItem(
                                    videoId    = song.videoId,
                                    title      = song.title,
                                    artist     = song.artist,
                                    thumbnail  = song.thumbnailUrl,
                                    duration   = song.duration,
                                    isPlaying  = state.currentSong?.videoId == song.videoId && state.isPlaying,
                                    isFavorite = true,
                                    onPlay = {
                                        vm.playSong(
                                            SearchResult(song.videoId, song.title, song.artist, song.thumbnailUrl, song.duration)
                                        )
                                    },
                                    onFavorite = { vm.removeFavorite(song) }
                                )
                            }
                        }
                    }
                }
                results.isEmpty() && query.isNotBlank() -> {
                    EmptyState("لا نتائج لـ \"$query\"")
                }
                results.isEmpty() -> {
                    WelcomeState()
                }
                else -> {
                    LazyColumn {
                        items(results, key = { it.videoId }) { song ->
                            SongItem(
                                videoId    = song.videoId,
                                title      = song.title,
                                artist     = song.artist,
                                thumbnail  = song.thumbnailUrl,
                                duration   = song.duration,
                                isPlaying  = state.currentSong?.videoId == song.videoId && state.isPlaying,
                                isFavorite = false,
                                onPlay     = { vm.playSong(song) },
                                onFavorite = { vm.addFavorite(song) }
                            )
                        }
                    }
                }
            }
        }

        // ── Error ────────────────────────────────────────────────────────
        state.error?.let { err ->
            Text(
                text = err,
                color = RedPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor)
                    .padding(12.dp)
            )
        }

        // ── Player Bar ───────────────────────────────────────────────────
        if (state.currentSong != null) {
            PlayerBar(state = state, onToggle = vm::togglePlayPause)
        }
    }
}

// ── Song Item ────────────────────────────────────────────────────────────────
@Composable
fun SongItem(
    videoId: String, title: String, artist: String,
    thumbnail: String, duration: Long,
    isPlaying: Boolean, isFavorite: Boolean,
    onPlay: () -> Unit, onFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .background(if (isPlaying) SurfaceVariant else BgColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            AsyncImage(
                model = thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.DarkGray)
            )
            if (isPlaying) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = RedPrimary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isPlaying) RedPrimary else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artist,
                color = TextSecondary,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(
                text = formatDuration(duration),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        IconButton(onClick = onFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) RedPrimary else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
    HorizontalDivider(color = SurfaceVariant, thickness = 0.5.dp)
}

@Composable
fun EmptyState(msg: String) = Box(Modifier.fillMaxSize(), Alignment.Center) {
    Text(text = msg, color = TextSecondary, fontSize = 16.sp)
}

@Composable
fun WelcomeState() = Box(Modifier.fillMaxSize(), Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎵", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("Muzik Tube", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("ابحث عن أي أغنية", color = TextSecondary, fontSize = 16.sp)
    }
}

fun formatDuration(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}
