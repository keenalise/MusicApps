package com.example.musicc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.musicc.model.Playlist
import com.example.musicc.model.Song
import com.example.musicc.ui.theme.*


@Composable
fun HomeScreen(
    songs: List<Song>,
    isLoading: Boolean = false,
    onPlaylistClick: (Playlist) -> Unit = {},
    onSongClick: (Song) -> Unit = {}
) {
    // Group songs by album for playlists
    val albumGroups = songs.groupBy { it.album }
    val playlists = albumGroups.map { (album, albumSongs) ->
        Playlist(
            id = album,
            name = album,
            description = "${albumSongs.first().artist} • ${albumSongs.size} songs",
            songs = albumSongs
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BackgroundDark, BackgroundDarker))),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Good evening",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            }
        } else if (songs.isEmpty()) {
            item {
                Text(
                    text = "No songs found. Please add some music to your device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // Recently Played Grid
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    songs.take(6).chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { song ->
                                RecentlyPlayedItem(
                                    song = song,
                                    onClick = { onSongClick(song) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Add empty space if odd number
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // All Songs Section
            if (songs.size > 6) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "All Songs",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(songs.drop(6)) { song ->
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) }
                    )
                }
            }

            // Albums Section
            if (playlists.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Albums",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(playlists.take(10)) { playlist ->
                            PlaylistCard(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentlyPlayedItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(60.dp)
                .background(PrimaryBlue.copy(alpha = 0.2f)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp),
            maxLines = 1
        )
    }
}

@Composable
fun SongListItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 1
            )
        }

        Text(
            text = song.duration,
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        // Album art from first song
        AsyncImage(
            model = playlist.songs.firstOrNull()?.albumArtUri,
            contentDescription = "Playlist Cover",
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryBlue.copy(alpha = 0.1f)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimary,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = playlist.description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 2
        )
    }
}

