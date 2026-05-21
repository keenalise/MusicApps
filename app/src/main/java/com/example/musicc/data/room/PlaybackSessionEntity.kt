package com.example.musicc.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a persisted playback session (a saved queue + playback state).
 * Room requires mutable properties (var) so it can set values during deserialization.
 */
@Entity(
    tableName = "playback_sessions",
    indices = [Index(value = ["is_active"]), Index(value = ["updated_at"]) ]
)
data class PlaybackSessionEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "title")
    var title: String? = null,

    @ColumnInfo(name = "created_at")
    var createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis(),

    // Current index in the queue (0-based). -1 if none selected
    @ColumnInfo(name = "current_index")
    var currentIndex: Int = -1,

    // Last known playback position in milliseconds
    @ColumnInfo(name = "last_position_ms")
    var lastPositionMs: Long = 0L,

    // simple playback state mapping: 0=STOPPED,1=PAUSED,2=PLAYING
    @ColumnInfo(name = "playback_state")
    var playbackState: Int = 0,

    @ColumnInfo(name = "repeat_mode")
    var repeatMode: Int = 0,

    @ColumnInfo(name = "shuffle_mode_enabled")
    var shuffleModeEnabled: Boolean = false,

    @ColumnInfo(name = "is_active")
    var isActive: Boolean = false
)


