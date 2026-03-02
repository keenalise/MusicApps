package com.example.musicc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.example.musicc.model.Song
import com.example.musicc.repository.MusicRepository
import com.example.musicc.service.MusicPlayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MusicRepository(application)
    private val playerService = MusicPlayerService(application)

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
        // Observe player state
        viewModelScope.launch {
            playerService.currentSong.collect { song ->
                _currentSong.value = song
            }
        }

        viewModelScope.launch {
            playerService.isPlaying.collect { playing ->
                _isPlaying.value = playing
            }
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val songs = repository.getAllSongs()
                _allSongs.value = songs
                if (songs.isNotEmpty()) {
                    playerService.setPlaylist(songs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun playSong(song: Song) {
        playerService.playSong(song)
        _currentSong.value = song
    }

    fun togglePlayPause() {
        playerService.togglePlayPause()
    }

    fun skipToNext() {
        playerService.skipToNext()
    }

    fun skipToPrevious() {
        playerService.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerService.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun toggleShuffle() {
        val newShuffleState = !_shuffleEnabled.value
        _shuffleEnabled.value = newShuffleState
        playerService.setShuffleMode(newShuffleState)
    }

    fun toggleRepeat() {
        val newRepeatMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = newRepeatMode
        playerService.setRepeatMode(newRepeatMode)
    }

    fun getCurrentPosition(): Long {
        return playerService.getCurrentPosition()
    }

    fun getDuration(): Long {
        return playerService.getDuration()
    }

    override fun onCleared() {
        super.onCleared()
        playerService.release()
    }
}

