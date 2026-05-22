package com.example.musicc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicc.data.room.AppDatabase
import com.example.musicc.data.room.PlaybackSessionEntity
import com.example.musicc.data.room.QueueItemEntity
import com.example.musicc.data.repo.SessionRepositoryImpl
import com.example.musicc.service.SessionManager
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for managing playback sessions.
 * Handles creating, switching, deleting, and persisting sessions.
 * Integrates with SessionManager for actual playback control.
 */
class SessionManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionRepository = SessionRepositoryImpl(
        db.playbackSessionDao(),
        db.queueItemDao()
    )

    // Initialize ExoPlayer and SessionManager
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(application).build()
    }

    private val sessionManager: SessionManager by lazy {
        SessionManager(sessionRepository, exoPlayer, viewModelScope)
    }

    private val _allSessions = MutableStateFlow<List<PlaybackSessionEntity>>(emptyList())
    val allSessions: StateFlow<List<PlaybackSessionEntity>> = _allSessions.asStateFlow()

    private val _activeSession = MutableStateFlow<PlaybackSessionEntity?>(null)
    val activeSession: StateFlow<PlaybackSessionEntity?> = _activeSession.asStateFlow()

    private val _isCreatingSession = MutableStateFlow(false)
    val isCreatingSession: StateFlow<Boolean> = _isCreatingSession.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Observe all sessions from repository
        viewModelScope.launch {
            sessionRepository.sessionsFlow().collect { sessions ->
                _allSessions.value = sessions.map { domain ->
                    PlaybackSessionEntity(
                        id = domain.id,
                        title = domain.title,
                        createdAt = domain.createdAt,
                        updatedAt = domain.updatedAt,
                        currentIndex = domain.currentIndex,
                        lastPositionMs = domain.lastPositionMs,
                        playbackState = when(domain.playbackState) {
                            com.example.musicc.domain.PlaybackState.PLAYING -> 2
                            com.example.musicc.domain.PlaybackState.PAUSED -> 1
                            else -> 0
                        },
                        repeatMode = domain.repeatMode,
                        shuffleModeEnabled = domain.shuffleModeEnabled,
                        isActive = domain.isActive
                    )
                }
            }
        }

        // Observe active session
        viewModelScope.launch {
            sessionRepository.sessionsFlow().collect { sessions ->
                _activeSession.value = sessions
                    .filter { it.isActive }
                    .firstOrNull()
                    ?.let { domain ->
                        PlaybackSessionEntity(
                            id = domain.id,
                            title = domain.title,
                            createdAt = domain.createdAt,
                            updatedAt = domain.updatedAt,
                            currentIndex = domain.currentIndex,
                            lastPositionMs = domain.lastPositionMs,
                            playbackState = when(domain.playbackState) {
                                com.example.musicc.domain.PlaybackState.PLAYING -> 2
                                com.example.musicc.domain.PlaybackState.PAUSED -> 1
                                else -> 0
                            },
                            repeatMode = domain.repeatMode,
                            shuffleModeEnabled = domain.shuffleModeEnabled,
                            isActive = domain.isActive
                        )
                    }
            }
        }
    }

    /**
     * Create a new empty session with the given title.
     */
    fun createNewSession(title: String) {
        viewModelScope.launch {
            try {
                _isCreatingSession.value = true
                _errorMessage.value = null

                sessionRepository.createSession(
                    title = title.ifEmpty { "Session ${System.currentTimeMillis()}" },
                    initialQueue = emptyList()
                )

            } catch (e: Exception) {
                _errorMessage.value = "Failed to create session: ${e.message}"
                e.printStackTrace()
            } finally {
                _isCreatingSession.value = false
            }
        }
    }

    /**
     * Switch to a different session, saving current state and loading the target session with its queue.
     * This integrates with SessionManager for seamless playback switching.
     */
    fun switchToSession(sessionId: Long, autoPlay: Boolean = false) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                // Use SessionManager to atomically switch sessions
                // This saves current session state and loads the new session
                sessionManager.switchToSession(sessionId, autoPlay = autoPlay)

            } catch (e: Exception) {
                _errorMessage.value = "Failed to switch session: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Delete a session and all its associated queue items.
     */
    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                sessionRepository.deleteSession(sessionId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete session: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Rename an existing session.
     */
    fun renameSession(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val session = db.playbackSessionDao().observeById(sessionId).first()
                if (session != null) {
                    val updated = session.copy(
                        title = newTitle,
                        updatedAt = System.currentTimeMillis()
                    )
                    db.playbackSessionDao().update(updated)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to rename session: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Save queue items for a specific session.
     */
    fun updateSessionQueue(sessionId: Long, queueItems: List<QueueItemEntity>) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                db.queueItemDao().replaceQueue(sessionId, queueItems)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update queue: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    /**
     * Save the current playback state before destroying the ViewModel.
     */
    override fun onCleared() {
        viewModelScope.launch {
            try {
                sessionManager.saveActiveSessionState()
                sessionManager.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onCleared()
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

