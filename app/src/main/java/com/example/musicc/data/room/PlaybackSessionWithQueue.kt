package com.example.musicc.data.room

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Aggregated relation: a PlaybackSession with its ordered list of QueueItemEntity.
 */
data class PlaybackSessionWithQueue(
    @Embedded
    val session: PlaybackSessionEntity,

    @Relation(parentColumn = "id", entityColumn = "session_id")
    val queue: List<QueueItemEntity>
)

