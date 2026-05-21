package com.example.musicc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicc.data.room.AppDatabase
import com.example.musicc.data.room.PlaybackSessionEntity
import com.example.musicc.data.room.QueueItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for managing playback sessions.
 * Handles creating, switching, deleting, and persisting sessions.
 */
class SessionManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _allSessions = MutableStateFlow<List<PlaybackSessionEntity>>(emptyList())
    val allSessions: StateFlow<List<PlaybackSessionEntity>> = _allSessions.asStateFlow()

    private val _activeSession = MutableStateFlow<PlaybackSessionEntity?>(null)
    val activeSession: StateFlow<PlaybackSessionEntity?> = _activeSession.asStateFlow()

    private val _isCreatingSession = MutableStateFlow(false)
    val isCreatingSession: StateFlow<Boolean> = _isCreatingSession.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Observe all sessions
        viewModelScope.launch {
            db.playbackSessionDao().observeAll().collect { sessions ->
                _allSessions.value = sessions
            }
        }

        // Observe active session
        viewModelScope.launch {
            db.playbackSessionDao().observeAll().collect { sessions ->
                _activeSession.value = sessions.firstOrNull { it.isActive }
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

                // Create new session entity
                val newSession = PlaybackSessionEntity(
                    title = title.ifEmpty { "Session ${System.currentTimeMillis()}" },
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isActive = false
                )

                // Save to database
                db.playbackSessionDao().insert(newSession)

            } catch (e: Exception) {
                _errorMessage.value = "Failed to create session: ${e.message}"
                e.printStackTrace()
            } finally {
                _isCreatingSession.value = false
            }
        }
    }

    /**
     * Switch to a different session, saving the current state and restoring the target session.
     */
    fun switchToSession(sessionId: Long, currentPosition: Long, playbackState: Int) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null

                // Get the current active session and save its state
                val currentActive = _activeSession.value
                if (currentActive != null) {
                    val updatedCurrentSession = currentActive.copy(
                        lastPositionMs = currentPosition,
                        playbackState = playbackState,
                        updatedAt = System.currentTimeMillis()
                    )
                    db.playbackSessionDao().update(updatedCurrentSession)
                }

                // Deactivate all other sessions and activate this one
                db.playbackSessionDao().clearActiveFlagsExcept(sessionId)

                // Load and update the target session
                val targetSession = db.playbackSessionDao().observeById(sessionId).first()
                if (targetSession != null) {
                    val updatedSession = targetSession.copy(
                        isActive = true,
                        updatedAt = System.currentTimeMillis()
                    )
                    db.playbackSessionDao().update(updatedSession)
                }

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
                db.playbackSessionDao().delete(sessionId)
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
    @Suppress("unused")
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
     * Clear error message.
     */
    @Suppress("unused")
    fun clearError() {
        _errorMessage.value = null
    }
}

