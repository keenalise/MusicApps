package com.example.musicc.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "queue_items",
    foreignKeys = [ForeignKey(
        entity = PlaybackSessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["session_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["session_id"]), Index(value = ["session_id", "position_in_queue"], unique = true)]
)
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "session_id")
    var sessionId: Long = 0,

    // stable media id (from MediaStore or generated)
    @ColumnInfo(name = "media_id")
    var mediaId: String = "",

    // content:// or http(s) URI as string
    @ColumnInfo(name = "uri")
    var uri: String = "",

    @ColumnInfo(name = "title")
    var title: String? = null,

    @ColumnInfo(name = "artist")
    var artist: String? = null,

    @ColumnInfo(name = "album")
    var album: String? = null,

    @ColumnInfo(name = "duration_ms")
    var durationMs: Long? = null,

    @ColumnInfo(name = "position_in_queue")
    var positionInQueue: Int = 0,

    // optional extras serialized as JSON (don't store secrets)
    @ColumnInfo(name = "extras_json")
    var extrasJson: String? = null
)


