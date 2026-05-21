package com.example.musicc.data.repo

import com.example.musicc.domain.PlaybackSession
import com.example.musicc.domain.QueueItem
import kotlinx.coroutines.flow.Flow

interface ISessionRepository {
    fun sessionsFlow(): Flow<List<PlaybackSession>>
    fun sessionFlow(sessionId: Long): Flow<PlaybackSession?>
    suspend fun createSession(title: String?, initialQueue: List<QueueItem> = emptyList()): Long
    suspend fun deleteSession(sessionId: Long)
    suspend fun setActiveSession(sessionId: Long?)
    suspend fun replaceQueue(sessionId: Long, items: List<QueueItem>)
    suspend fun savePlaybackState(sessionId: Long, currentIndex: Int, positionMs: Long, playbackState: Int)
    suspend fun getActiveSessionId(): Long?
}

