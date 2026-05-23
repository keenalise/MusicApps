package com.example.musicc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.musicc.AppProvider
import com.example.musicc.domain.QueueItem
import com.example.musicc.model.Song
import com.example.musicc.repository.MusicRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val appProvider = application as AppProvider
    private val repository = MusicRepository(application)
    private val sessionManager = appProvider.sessionManager
    private val player = appProvider.player

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
                // Find matching song in our loaded list
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
                _allSongs.value = songs
                updateCurrentSongFromPlayer()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            val currentSession = sessionManager.currentSession.value
            if (currentSession != null) {
                // To keep it simple, if user clicks a song on Home, we replace the entire queue of the ACTIVE session
                // with all songs, and start playing at this song's index.
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
                sessionManager.replaceQueueSafe(currentSession.id, queueItems, startIndex, true)
            }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipToNext() {
        player.seekToNext()
    }

    fun skipToPrevious() {
        player.seekToPrevious()
    }

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

    override fun onCleared() {
        // We don't release the player here as it's a singleton in AppProvider
        super.onCleared()
    }
}
