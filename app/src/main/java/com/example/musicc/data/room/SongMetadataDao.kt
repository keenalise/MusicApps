package com.example.musicc.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongMetadataDao {
    @Query("SELECT * FROM song_metadata WHERE songId = :songId")
    fun observeMetadata(songId: String): Flow<SongMetadataEntity?>

    @Query("SELECT * FROM song_metadata")
    fun observeAllMetadata(): Flow<List<SongMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(metadata: SongMetadataEntity)

    @Query("UPDATE song_metadata SET custom_title = :title WHERE songId = :songId")
    suspend fun updateTitle(songId: String, title: String)

    @Query("UPDATE song_metadata SET custom_cover_uri = :coverUri WHERE songId = :songId")
    suspend fun updateCover(songId: String, coverUri: String)

    @Query("UPDATE song_metadata SET is_favorite = :isFavorite WHERE songId = :songId")
    suspend fun updateFavorite(songId: String, isFavorite: Boolean)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun observeById(playlistId: Long): Flow<PlaylistEntity?>

    @Insert
    suspend fun insert(playlist: PlaylistEntity): Long

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Delete
    suspend fun delete(playlist: PlaylistEntity)

    @Transaction
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    fun observeSongsInPlaylist(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getSongsInPlaylist(playlistId: Long): List<PlaylistSongEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(playlistSong: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String)
}
