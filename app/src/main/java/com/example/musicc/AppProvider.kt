package com.example.musicc

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicc.data.repo.ISessionRepository
import com.example.musicc.data.repo.SessionRepositoryImpl
import com.example.musicc.data.room.AppDatabase
import com.example.musicc.repository.SongMetadataRepository
import com.example.musicc.service.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Minimal Application provider to construct Room DB and repository.
 * Provides a single ExoPlayer and SessionManager instance for the entire app.
 */
class AppProvider : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    lateinit var sessionRepository: ISessionRepository
        private set

    lateinit var player: ExoPlayer
        private set

    lateinit var sessionManager: SessionManager
        private set

    lateinit var metadataRepository: SongMetadataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        sessionRepository = SessionRepositoryImpl(database.playbackSessionDao(), database.queueItemDao())
        metadataRepository = SongMetadataRepository(database.songMetadataDao(), database.playlistDao())
        
        player = ExoPlayer.Builder(this).build()
        sessionManager = SessionManager(sessionRepository, player, appScope)
    }
}
