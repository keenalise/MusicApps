package com.example.musicc

import android.app.Application
import androidx.room.Room
import com.example.musicc.data.repo.SessionRepositoryImpl
import com.example.musicc.data.room.AppDatabase

/**
 * Minimal Application provider to construct Room DB and repository.
 * In production, replace with proper DI (Hilt/Koin).
 */
class AppProvider : Application() {
    lateinit var database: AppDatabase
        private set

    lateinit var sessionRepository: com.example.musicc.data.repo.ISessionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "music_app.db").build()
        sessionRepository = SessionRepositoryImpl(database.playbackSessionDao(), database.queueItemDao())
    }
}

