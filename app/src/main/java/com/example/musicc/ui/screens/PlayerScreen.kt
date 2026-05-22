package com.example.musicc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.musicc.model.Song
import com.example.musicc.ui.theme.*


@Composable
fun PlayerScreen(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onBackClick: () -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {}
) {
    var isFavorite by remember { mutableStateOf(false) }
    val duration = song.durationMs
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = listOf(BackgroundDark, BackgroundDarker)))
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Text(
                text = song.album,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Album Art
        AsyncImage(
            model = song.albumArtUri,
            contentDescription = "Album Art",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryPurple.copy(alpha = 0.3f), PrimaryBlue.copy(alpha = 0.3f))
                    )
                ),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Song Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }

            IconButton(onClick = { isFavorite = !isFavorite }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) AccentPink else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Slider
        Column {
            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val newPosition = (newProgress * duration).toLong()
                    onSeek(newPosition)
                },
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryBlue,
                    activeTrackColor = PrimaryBlue,
                    inactiveTrackColor = SurfaceLight
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
                Text(
                    text = song.duration,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleEnabled) AccentCyan else TextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }

            IconButton(onClick = onSkipPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            FloatingActionButton(
                onClick = onPlayPauseClick,
                containerColor = PrimaryBlue,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = TextPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = onToggleRepeat) {
                Icon(
                    imageVector = when (repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) AccentCyan else TextTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Additional Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.DeviceHub,
                    contentDescription = "Connect to a device",
                    tint = TextTertiary
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = TextTertiary
                )
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val seconds = (timeMs / 1000) % 60
    val minutes = (timeMs / (1000 * 60)) % 60
    val hours = (timeMs / (1000 * 60 * 60))

    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
    }
}

