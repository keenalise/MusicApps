package com.example.musicc.data.repo

import com.example.musicc.data.room.PlaybackSessionDao
import com.example.musicc.data.room.PlaybackSessionEntity
import com.example.musicc.data.room.QueueItemDao
import com.example.musicc.domain.PlaybackSession
import com.example.musicc.domain.QueueItem
import com.example.musicc.domain.toDomain
import com.example.musicc.domain.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val playbackSessionDao: PlaybackSessionDao,
    private val queueItemDao: QueueItemDao
) : ISessionRepository {

    override fun sessionsFlow(): Flow<List<PlaybackSession>> =
        playbackSessionDao.observeAll().map { list ->
            list.map { entity -> PlaybackSession(
                id = entity.id,
                title = entity.title,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
                currentIndex = entity.currentIndex,
                lastPositionMs = entity.lastPositionMs,
                playbackState = when(entity.playbackState){ 2 -> com.example.musicc.domain.PlaybackState.PLAYING; 1 -> com.example.musicc.domain.PlaybackState.PAUSED; else -> com.example.musicc.domain.PlaybackState.STOPPED },
                repeatMode = entity.repeatMode,
                shuffleModeEnabled = entity.shuffleModeEnabled,
                isActive = entity.isActive
            ) }
        }

    override fun sessionFlow(sessionId: Long): Flow<PlaybackSession?> =
        playbackSessionDao.observeSessionWithQueue(sessionId).map { withQueue ->
            withQueue?.let { pwq ->
                pwq.session.toDomain(pwq.queue)
            }
        }

    override suspend fun createSession(title: String?, initialQueue: List<QueueItem>): Long =
        withContext(Dispatchers.IO) {
            val entity = PlaybackSessionEntity(title = title ?: "New Session", isActive = false)
            val sessionId = playbackSessionDao.insert(entity)
            if (initialQueue.isNotEmpty()) {
                val items = initialQueue.map { it.toEntity(sessionId) }
                queueItemDao.insertAll(items)
            }
            sessionId
        }

    override suspend fun deleteSession(sessionId: Long) {
        withContext(Dispatchers.IO) {
            playbackSessionDao.delete(sessionId)
        }
    }

    override suspend fun setActiveSession(sessionId: Long?) {
        withContext(Dispatchers.IO) {
            playbackSessionDao.clearActiveFlagsExcept(sessionId)
            sessionId?.let { id ->
                val entity = playbackSessionDao.observeById(id).filterNotNull().first()
                val updated = entity.copy(isActive = true, updatedAt = System.currentTimeMillis())
                playbackSessionDao.update(updated)
            }
        }
    }

    override suspend fun replaceQueue(sessionId: Long, items: List<QueueItem>) {
        withContext(Dispatchers.IO) {
            val entities = items.mapIndexed { index, item -> item.copy(positionInQueue = index).toEntity(sessionId) }
            queueItemDao.replaceQueue(sessionId, entities)
        }
    }

    override suspend fun savePlaybackState(sessionId: Long, currentIndex: Int, positionMs: Long, playbackState: Int) {
        withContext(Dispatchers.IO) {
            val entity = playbackSessionDao.observeById(sessionId).filterNotNull().first()
            val updated = entity.copy(currentIndex = currentIndex, lastPositionMs = positionMs, playbackState = playbackState, updatedAt = System.currentTimeMillis())
            playbackSessionDao.update(updated)
        }
    }

    override suspend fun getActiveSessionId(): Long? =
        withContext(Dispatchers.IO) {
            playbackSessionDao.observeAll().first()
                .firstOrNull { it.isActive }?.id
        }
}


