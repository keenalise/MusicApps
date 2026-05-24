package com.example.musicc.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song_metadata")
data class SongMetadataEntity(
    @PrimaryKey
    val songId: String,
    
    @ColumnInfo(name = "custom_title")
    val customTitle: String? = null,
    
    @ColumnInfo(name = "custom_cover_uri")
    val customCoverUri: String? = null,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "custom_cover_uri")
    val customCoverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: String,
    val addedAt: Long = System.currentTimeMillis()
)
