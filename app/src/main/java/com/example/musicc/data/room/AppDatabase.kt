package com.example.musicc.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlaybackSessionEntity::class, QueueItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playbackSessionDao(): PlaybackSessionDao
    abstract fun queueItemDao(): QueueItemDao
}

