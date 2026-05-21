package com.example.musicc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicc.domain.PlaybackSession
import com.example.musicc.service.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentSession = MutableStateFlow<PlaybackSession?>(null)
    val currentSession: StateFlow<PlaybackSession?> = _currentSession.asStateFlow()

    init {
        // Observe session manager's current session
        viewModelScope.launch {
            sessionManager.currentSession.collect { session ->
                _currentSession.value = session
            }
        }
    }

    fun play() {
        viewModelScope.launch { sessionManager.loadSession(sessionManager.currentSession.value?.id ?: return@launch) }
    }

    fun pause() {
        // Pause via player in SessionManager
        viewModelScope.launch { sessionManager.seekTo(0L) }
    }

    fun switchSession(sessionId: Long, autoPlay: Boolean = false) {
        viewModelScope.launch { sessionManager.switchToSession(sessionId, autoPlay) }
    }

    fun saveNow() {
        viewModelScope.launch { sessionManager.saveActiveSessionState() }
    }
}

