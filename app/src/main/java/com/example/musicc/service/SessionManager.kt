package com.example.musicc.service

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicc.data.repo.ISessionRepository
import com.example.musicc.domain.QueueItem
import com.example.musicc.domain.PlaybackSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Manages session switching and synchronizes ExoPlayer with persisted sessions.
 * Ensures only one ExoPlayer instance is used and queue replacements are atomic.
 */
class SessionManager(
    private val repository: ISessionRepository,
    private val player: ExoPlayer,
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()

    private val _currentSession = MutableStateFlow<PlaybackSession?>(null)
    val currentSession: StateFlow<PlaybackSession?> = _currentSession

    init {
        // Basic player configuration can be done here (repeat/shuffle modes are applied per session)
        try {
            player.repeatMode = androidx.media3.common.Player.REPEAT_MODE_OFF
        } catch (_: Throwable) {
            // ignore configuration failure in environments without ExoPlayer
        }
    }

    private fun validateUri(uriString: String): Uri? {
        return try {
            val uri = uriString.toUri()
            val scheme = uri.scheme
            if (scheme == "content" || scheme == "http" || scheme == "https") uri else null
        } catch (_: Exception) { null }
    }

    /**
     * Save current player's state back to the active session in DB.
     */
    suspend fun saveActiveSessionState() {
        // Read player state on Main/UI thread, then persist on IO
        val session = _currentSession.value ?: return
        val idx: Int
        val pos: Long
        val stateInt: Int
        try {
            idx = player.currentMediaItemIndex
            pos = player.currentPosition
            stateInt = if (player.isPlaying) 2 else if (player.playbackState == androidx.media3.common.Player.STATE_READY) 1 else 0
        } catch (_: Throwable) {
            // In test/non-android environment, default values
            return
        }

        withContext(Dispatchers.IO) {
            repository.savePlaybackState(session.id, idx, pos, stateInt)
        }
    }

    /**
     * Load session snapshot (does not modify player) and update internal state.
     */
    suspend fun loadSession(sessionId: Long): PlaybackSession? {
        val session = withContext(Dispatchers.IO) {
            repository.sessionFlow(sessionId).firstOrNull()
        }
        _currentSession.value = session
        return session
    }

    /**
     * Switch current playback to another session atomically: save previous, replace queue, seek to position.
     */
    suspend fun switchToSession(sessionId: Long, autoPlay: Boolean = false) {
        mutex.withLock {
            // Persist current state (if any)
            try {
                saveActiveSessionState()
            } catch (_: Exception) { /* ignore */ }

            // Load new session snapshot from DB
            val session = withContext(Dispatchers.IO) { repository.sessionFlow(sessionId).firstOrNull() } ?: return

            // Build MediaItems safely
            val mediaItems = session.queue.mapNotNull { item ->
                val uri = validateUri(item.uri) ?: return@mapNotNull null
                MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(item.mediaId)
                    .setTag(item as Any)
                    .build()
            }

            // Replace player's queue and restore position on Main
            withContext(Dispatchers.Main) {
                try {
                    player.setMediaItems(mediaItems, /* resetPosition= */ true)
                    player.prepare()

                    if (mediaItems.isNotEmpty()) {
                        val index = if (session.currentIndex in 0 until mediaItems.size) session.currentIndex else 0
                        val position = if (session.lastPositionMs >= 0) session.lastPositionMs else 0L
                        player.seekTo(index, position)

                        if (autoPlay) player.play() else player.pause()
                    } else {
                        player.pause()
                    }

                } catch (_: Throwable) {
                    // ignore player operations failures in non-android/test env
                }
            }

            _currentSession.value = session

            // Mark active in repository
            withContext(Dispatchers.IO) {
                repository.setActiveSession(sessionId)
            }
        }
    }

    /**
     * Replace queue contents of a session and optionally start playing at startIndex.
     */
    suspend fun replaceQueueSafe(sessionId: Long, items: List<QueueItem>, startIndex: Int = 0, autoPlay: Boolean = false) {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                repository.replaceQueue(sessionId, items)
            }

            if (_currentSession.value?.id == sessionId) {
                val mediaItems = items.mapNotNull { itItem ->
                    val uri = validateUri(itItem.uri) ?: return@mapNotNull null
                    MediaItem.Builder().setUri(uri).setMediaId(itItem.mediaId).setTag(itItem as Any).build()
                }

                withContext(Dispatchers.Main) {
                    try {
                        player.setMediaItems(mediaItems, /* resetPosition= */ true)
                        player.prepare()
                        if (mediaItems.isNotEmpty()) {
                            val idx = startIndex.coerceIn(0, (mediaItems.size - 1).coerceAtLeast(0))
                            player.seekTo(idx, 0L)
                            if (autoPlay) player.play() else player.pause()
                        } else {
                            player.pause()
                        }
                    } catch (_: Throwable) { }
                }
            }
        }
    }

    suspend fun seekTo(positionMs: Long) {
        withContext(Dispatchers.Main) {
            try { player.seekTo(positionMs) } catch (_: Throwable) { }
        }
    }

    suspend fun release() {
        mutex.withLock {
            try { saveActiveSessionState() } catch (_: Throwable) { }
            try { player.release() } catch (_: Throwable) { }
        }
    }
}
