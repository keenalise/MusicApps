package com.example.musicc.repository

import com.example.musicc.data.room.PlaylistDao
import com.example.musicc.data.room.PlaylistEntity
import com.example.musicc.data.room.PlaylistSongEntity
import com.example.musicc.data.room.SongMetadataDao
import com.example.musicc.data.room.SongMetadataEntity
import kotlinx.coroutines.flow.Flow

class SongMetadataRepository(
    private val metadataDao: SongMetadataDao,
    private val playlistDao: PlaylistDao
) {
    fun observeMetadata(songId: String) = metadataDao.observeMetadata(songId)
    fun observeAllMetadata() = metadataDao.observeAllMetadata()
    fun observePlaylists() = playlistDao.observeAll()

    suspend fun updateTitle(songId: String, title: String) {
        val current = metadataDao.observeMetadata(songId).let { /* not efficient to collect here */ }
        metadataDao.insertOrUpdate(SongMetadataEntity(songId = songId, customTitle = title))
    }

    suspend fun updateFavorite(songId: String, isFavorite: Boolean) {
        metadataDao.insertOrUpdate(SongMetadataEntity(songId = songId, isFavorite = isFavorite))
    }

    suspend fun updateCover(songId: String, coverUri: String) {
        metadataDao.insertOrUpdate(SongMetadataEntity(songId = songId, customCoverUri = coverUri))
    }

    suspend fun createPlaylist(name: String): Long = playlistDao.insert(PlaylistEntity(name = name))

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        playlistDao.addSongToPlaylist(PlaylistSongEntity(playlistId, songId))
    }
}
