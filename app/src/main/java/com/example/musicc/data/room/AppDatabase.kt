package com.example.musicc.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaybackSessionEntity::class, 
        QueueItemEntity::class, 
        SongMetadataEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playbackSessionDao(): PlaybackSessionDao
    abstract fun queueItemDao(): QueueItemDao
    abstract fun songMetadataDao(): SongMetadataDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "musicc_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
