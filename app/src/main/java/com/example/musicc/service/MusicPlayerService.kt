package com.example.musicc.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicc.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayerService(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private var playlist: List<Song> = emptyList()
    private var currentIndex: Int = -1

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Update current song when track changes
                if (currentIndex >= 0 && currentIndex < playlist.size) {
                    _currentSong.value = playlist[currentIndex]
                }
            }
        })
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        playlist = songs
        currentIndex = startIndex

        val mediaItems = songs.mapNotNull { song ->
            song.uri?.let { MediaItem.fromUri(it) }
        }

        player.setMediaItems(mediaItems, startIndex, 0)
        player.prepare()

        if (startIndex >= 0 && startIndex < playlist.size) {
            _currentSong.value = playlist[startIndex]
        }
    }

    fun playSong(song: Song) {
        val index = playlist.indexOfFirst { it.id == song.id }
        if (index != -1) {
            currentIndex = index
            player.seekTo(index, 0)
            player.play()
            _currentSong.value = song
        }
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun skipToNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            currentIndex++
            if (currentIndex < playlist.size) {
                _currentSong.value = playlist[currentIndex]
            }
        }
    }

    fun skipToPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            currentIndex--
            if (currentIndex >= 0) {
                _currentSong.value = playlist[currentIndex]
            }
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    fun getDuration(): Long {
        return player.duration
    }

    fun setRepeatMode(repeatMode: Int) {
        player.repeatMode = repeatMode
    }

    fun setShuffleMode(enabled: Boolean) {
        player.shuffleModeEnabled = enabled
    }

    fun release() {
        player.release()
    }
}

