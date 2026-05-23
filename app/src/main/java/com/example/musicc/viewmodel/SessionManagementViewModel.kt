package com.example.musicc.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicc.AppProvider
import com.example.musicc.data.room.PlaybackSessionEntity
import com.example.musicc.data.room.QueueItemEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SessionManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val appProvider = application as AppProvider
    private val sessionRepository = appProvider.sessionRepository
    private val sessionManager = appProvider.sessionManager
    private val db = appProvider.database

    private val _allSessions = MutableStateFlow<List<PlaybackSessionEntity>>(emptyList())
    val allSessions: StateFlow<List<PlaybackSessionEntity>> = _allSessions.asStateFlow()

    private val _activeSession = MutableStateFlow<PlaybackSessionEntity?>(null)
    val activeSession: StateFlow<PlaybackSessionEntity?> = _activeSession.asStateFlow()

    private val _isCreatingSession = MutableStateFlow(false)
    val isCreatingSession: StateFlow<Boolean> = _isCreatingSession.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Ensure default session exists and is active if none is
        viewModelScope.launch {
            val sessions = sessionRepository.sessionsFlow().first()
            if (sessions.isEmpty()) {
                val id = sessionRepository.createSession("Default Session")
                sessionManager.switchToSession(id)
            } else if (sessionRepository.getActiveSessionId() == null) {
                sessionManager.switchToSession(sessions.first().id)
            }
        }

        // Observe all sessions
        viewModelScope.launch {
            sessionRepository.sessionsFlow().collect { sessions ->
                _allSessions.value = sessions.map { it.toEntity() }
            }
        }

        // Observe active session from manager
        viewModelScope.launch {
            sessionManager.currentSession.collectLatest { session ->
                _activeSession.value = session?.toEntity()
            }
        }
    }

    private fun com.example.musicc.domain.PlaybackSession.toEntity(): PlaybackSessionEntity {
        return PlaybackSessionEntity(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            currentIndex = currentIndex,
            lastPositionMs = lastPositionMs,
            playbackState = when(playbackState) {
                com.example.musicc.domain.PlaybackState.PLAYING -> 2
                com.example.musicc.domain.PlaybackState.PAUSED -> 1
                else -> 0
            },
            repeatMode = repeatMode,
            shuffleModeEnabled = shuffleModeEnabled,
            isActive = isActive
        )
    }

    fun createNewSession(title: String) {
        viewModelScope.launch {
            try {
                _isCreatingSession.value = true
                sessionRepository.createSession(title.ifEmpty { "New Session" })
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create session: ${e.message}"
            } finally {
                _isCreatingSession.value = false
            }
        }
    }

    fun switchToSession(sessionId: Long, autoPlay: Boolean = false) {
        viewModelScope.launch {
            try {
                sessionManager.switchToSession(sessionId, autoPlay)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to switch: ${e.message}"
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            try {
                sessionRepository.deleteSession(sessionId)
                // If we deleted the active one, switch to first available
                val active = sessionRepository.getActiveSessionId()
                if (active == null) {
                    val remaining = sessionRepository.sessionsFlow().first()
                    if (remaining.isNotEmpty()) {
                        switchToSession(remaining.first().id)
                    } else {
                        createNewSession("Default Session")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun renameSession(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            try {
                val dao = db.playbackSessionDao()
                val session = dao.observeById(sessionId).first()
                session?.let {
                    dao.update(it.copy(title = newTitle, updatedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to rename: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
