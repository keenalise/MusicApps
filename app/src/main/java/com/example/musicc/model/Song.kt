package com.example.musicc.model

import android.net.Uri

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val durationMs: Long = 0L,
    val uri: Uri? = null,
    val albumArtUri: Uri? = null,
    val imageRes: Int = 0,
    val isFavorite: Boolean = false
)

data class Playlist(
    val id: String,
    val name: String,
    val description: String,
    val imageRes: Int = 0,
    val songs: List<Song> = emptyList()
)
