package com.example.musicc.domain

import com.example.musicc.data.room.PlaybackSessionEntity
import com.example.musicc.data.room.QueueItemEntity

// Domain models used by higher layers

data class QueueItem(
    val id: Long = 0,
    val mediaId: String,
    val uri: String,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long? = null,
    val positionInQueue: Int = 0
)

data class PlaybackSession(
    val id: Long = 0,
    val title: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val currentIndex: Int = -1,
    val lastPositionMs: Long = 0L,
    val playbackState: PlaybackState = PlaybackState.STOPPED,
    val repeatMode: Int = 0,
    val shuffleModeEnabled: Boolean = false,
    val isActive: Boolean = false,
    val queue: List<QueueItem> = emptyList()
)

enum class PlaybackState { STOPPED, PAUSED, PLAYING }

// Simple mappers
fun PlaybackSessionEntity.toDomain(queue: List<QueueItemEntity>): PlaybackSession = PlaybackSession(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    currentIndex = currentIndex,
    lastPositionMs = lastPositionMs,
    playbackState = when(playbackState) { 2 -> PlaybackState.PLAYING; 1 -> PlaybackState.PAUSED; else -> PlaybackState.STOPPED },
    repeatMode = repeatMode,
    shuffleModeEnabled = shuffleModeEnabled,
    isActive = isActive,
    queue = queue.map { it.toDomain() }
)

fun QueueItemEntity.toDomain(): QueueItem = QueueItem(
    id = id,
    mediaId = mediaId,
    uri = uri,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    positionInQueue = positionInQueue
)

fun QueueItem.toEntity(sessionId: Long): QueueItemEntity = QueueItemEntity(
    id = id,
    sessionId = sessionId,
    mediaId = mediaId,
    uri = uri,
    title = title,
    artist = artist,
    album = album,
    durationMs = durationMs,
    positionInQueue = positionInQueue
)

fun PlaybackSession.toEntity(): PlaybackSessionEntity = PlaybackSessionEntity(
    id = id,
    title = title,
    createdAt = createdAt.takeIf { it != 0L } ?: System.currentTimeMillis(),
    updatedAt = updatedAt.takeIf { it != 0L } ?: System.currentTimeMillis(),
    currentIndex = currentIndex,
    lastPositionMs = lastPositionMs,
    playbackState = when(playbackState) { PlaybackState.PLAYING -> 2; PlaybackState.PAUSED -> 1; else -> 0 },
    repeatMode = repeatMode,
    shuffleModeEnabled = shuffleModeEnabled,
    isActive = isActive
)

