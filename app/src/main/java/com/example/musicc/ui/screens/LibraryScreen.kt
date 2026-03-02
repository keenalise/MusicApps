package com.example.musicc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.musicc.model.Playlist
import com.example.musicc.ui.theme.SpotifyGray
import com.example.musicc.ui.theme.SpotifyLightGray

@Composable
fun LibraryScreen(
    onPlaylistClick: (Playlist) -> Unit = {}
) {
    val playlists = listOf(
        Playlist("1", "Liked Songs", "125 songs"),
        Playlist("2", "My Playlist #1", "Your episodes"),
        Playlist("3", "Chill Vibes", "30 songs"),
        Playlist("4", "Workout Mix", "45 songs"),
        Playlist("5", "Road Trip", "60 songs"),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Library",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(onClick = { /* Create new playlist */ }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add playlist",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text("Playlists") }
            )
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text("Artists") }
            )
            FilterChip(
                selected = false,
                onClick = { },
                label = { Text("Albums") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playlists List
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistListItem(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) }
                )
            }
        }
    }
}

@Composable
fun PlaylistListItem(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Playlist cover
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SpotifyGray)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Playlist • ${playlist.description}",
                style = MaterialTheme.typography.bodyMedium,
                color = SpotifyLightGray
            )
        }
    }
}

