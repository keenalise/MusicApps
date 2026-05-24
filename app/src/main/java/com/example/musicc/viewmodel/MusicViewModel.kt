package com.example.musicc.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.musicc.AppProvider
import com.example.musicc.data.room.PlaylistEntity
import com.example.musicc.domain.QueueItem
import com.example.musicc.model.Song
import com.example.musicc.repository.MusicRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val appProvider = application as AppProvider
    private val repository = MusicRepository(application)
    private val sessionManager = appProvider.sessionManager
    private val player = appProvider.player
    private val metadataRepository = appProvider.metadataRepository
    private val database = appProvider.database

    private val _rawSongs = MutableStateFlow<List<Song>>(emptyList())
    
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList())
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
    val playlists: StateFlow<List<PlaylistEntity>> = _playlists.asStateFlow()

    init {
        // Synchronize with player state
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSongFromPlayer()
            }

            override fun onRepeatModeChanged(mode: Int) {
                _repeatMode.value = mode
            }

            override fun onShuffleModeEnabledChanged(enabled: Boolean) {
                _shuffleEnabled.value = enabled
            }
        })

        // Combine raw songs with metadata
        viewModelScope.launch {
            combine(_rawSongs, metadataRepository.observeAllMetadata()) { songs, metadata ->
                songs.map { song ->
                    val meta = metadata.find { it.songId == song.id }
                    song.copy(
                        title = meta?.customTitle ?: song.title,
                        albumArtUri = meta?.customCoverUri?.let { Uri.parse(it) } ?: song.albumArtUri,
                        isFavorite = meta?.isFavorite ?: false
                    )
                }
            }.collect { updatedSongs ->
                _allSongs.value = updatedSongs
                updateCurrentSongFromPlayer()
            }
        }

        // Observe playlists
        viewModelScope.launch {
            metadataRepository.observePlaylists().collect {
                _playlists.value = it
            }
        }

        // Periodically update position when playing
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                }
                delay(1000)
            }
        }
    }

    private fun updateCurrentSongFromPlayer() {
        val currentItem = player.currentMediaItem
        if (currentItem != null) {
            val tag = currentItem.localConfiguration?.tag as? QueueItem
            if (tag != null) {
                _currentSong.value = _allSongs.value.find { it.id == tag.mediaId }
            }
        } else {
            _currentSong.value = null
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val songs = repository.getAllSongs()
                _rawSongs.value = songs
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun ensureActiveSession(): Long {
        val currentSession = sessionManager.currentSession.value
        if (currentSession != null) return currentSession.id

        val sessions = appProvider.sessionRepository.sessionsFlow().first()
        val sessionId = sessions.find { it.title == "Default Session" }?.id 
            ?: appProvider.sessionRepository.createSession("Default Session")
        
        sessionManager.switchToSession(sessionId)
        return sessionId
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            val sessionId = ensureActiveSession()
            val queueItems = _allSongs.value.mapIndexed { index, s ->
                QueueItem(
                    mediaId = s.id,
                    uri = s.uri.toString(),
                    title = s.title,
                    artist = s.artist,
                    album = s.album,
                    positionInQueue = index
                )
            }
            val startIndex = _allSongs.value.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
            sessionManager.replaceQueueSafe(sessionId, queueItems, startIndex, true)
        }
    }

    fun playPlaylist(playlist: PlaylistEntity, shuffle: Boolean = false) {
        viewModelScope.launch {
            val sessionId = ensureActiveSession()
            val playlistSongs = database.playlistDao().getSongsInPlaylist(playlist.id)
            val songIds = playlistSongs.map { it.songId }.toSet()
            
            var songsToPlay = _allSongs.value.filter { it.id in songIds }
            
            if (shuffle) {
                songsToPlay = songsToPlay.shuffled()
            }

            if (songsToPlay.isNotEmpty()) {
                val queueItems = songsToPlay.mapIndexed { index, s ->
                    QueueItem(
                        mediaId = s.id,
                        uri = s.uri.toString(),
                        title = s.title,
                        artist = s.artist,
                        album = s.album,
                        positionInQueue = index
                    )
                }
                sessionManager.replaceQueueSafe(sessionId, queueItems, 0, true)
            }
        }
    }

    fun playPlaylistNext(playlistId: Long) {
        viewModelScope.launch {
            ensureActiveSession()
            val playlistSongs = database.playlistDao().getSongsInPlaylist(playlistId)
            val songIds = playlistSongs.map { it.songId }.toSet()
            val songsToPlay = _allSongs.value.filter { it.id in songIds }

            if (songsToPlay.isNotEmpty()) {
                val mediaItems = songsToPlay.map { song ->
                    MediaItem.Builder()
                        .setUri(song.uri)
                        .setMediaId(song.id)
                        .setTag(QueueItem(mediaId = song.id, uri = song.uri.toString(), title = song.title, artist = song.artist))
                        .build()
                }
                val currentIndex = player.currentMediaItemIndex
                val insertIndex = if (currentIndex == -1) 0 else currentIndex + 1
                player.addMediaItems(insertIndex, mediaItems)
            }
        }
    }

    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return database.playlistDao().observeSongsInPlaylist(playlistId).combine(_allSongs) { playlistSongs, all ->
            val songIds = playlistSongs.map { it.songId }.toSet()
            all.filter { it.id in songIds }
        }
    }

    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?> {
        return database.playlistDao().observeById(playlistId)
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            database.playlistDao().delete(playlist)
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String) {
        viewModelScope.launch {
            val playlist = database.playlistDao().observeById(playlistId).first()
            playlist?.let {
                database.playlistDao().update(it.copy(name = newName))
            }
        }
    }

    fun updatePlaylistCover(playlistId: Long, uri: Uri) {
        viewModelScope.launch {
            val playlist = database.playlistDao().observeById(playlistId).first()
            playlist?.let {
                database.playlistDao().update(it.copy(customCoverUri = uri.toString()))
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        viewModelScope.launch {
            database.playlistDao().removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            metadataRepository.updateFavorite(song.id, !song.isFavorite)
        }
    }

    fun renameSong(song: Song, newTitle: String) {
        viewModelScope.launch {
            metadataRepository.updateTitle(song.id, newTitle)
        }
    }

    fun updateCover(song: Song, uri: Uri) {
        viewModelScope.launch {
            metadataRepository.updateCover(song.id, uri.toString())
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            metadataRepository.createPlaylist(name)
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song) {
        viewModelScope.launch {
            metadataRepository.addSongToPlaylist(playlistId, song.id)
        }
    }

    fun shareSong(song: Song) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, song.uri)
            putExtra(Intent.EXTRA_SUBJECT, "Sharing Song: ${song.title}")
            putExtra(Intent.EXTRA_TEXT, "Check out this song: ${song.title} by ${song.artist}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Song")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(chooser)
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipToNext() { player.seekToNext() }
    fun skipToPrevious() { player.seekToPrevious() }
    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun toggleShuffle() {
        val next = !player.shuffleModeEnabled
        player.shuffleModeEnabled = next
        _shuffleEnabled.value = next
    }

    fun toggleRepeat() {
        val next = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = next
        _repeatMode.value = next
    }

    fun getCurrentPosition(): Long = player.currentPosition
    fun getDuration(): Long = player.duration
}
