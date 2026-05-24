package com.example.musicc.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicc.MainActivity
import com.example.musicc.R
import com.example.musicc.data.repo.SessionRepositoryImpl
import com.example.musicc.data.room.AppDatabase
import com.example.musicc.data.repo.ISessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Service that owns ExoPlayer and the SessionManager.
 * Keeps the service non-exported in Manifest for security.
 *
 * TODO: After Gradle sync, inherit from MediaSessionService instead of Service
 * and add MediaSession + MediaSessionConnector for full media controls.
 */
@UnstableApi
class MusicMediaService : Service() {

    private val serviceScope: CoroutineScope = MainScope()

    // These would be injected via DI in production; keeping simple here
    private lateinit var player: ExoPlayer
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: ISessionRepository

    override fun onCreate() {
        super.onCreate()

        // create player
        player = ExoPlayer.Builder(this).build()

        // Resolve repository defensively so the service does not crash the whole process on startup.
        repository = (application as? com.example.musicc.AppProvider)?.sessionRepository
            ?: SessionRepositoryImpl(
                AppDatabase.getDatabase(applicationContext).playbackSessionDao(),
                AppDatabase.getDatabase(applicationContext).queueItemDao()
            )

        sessionManager = SessionManager(repository, player, serviceScope)

        // Start foreground when playing
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                serviceScope.launch(Dispatchers.Main) {
                    if (isPlaying) startForegroundNotification() else {
                        @Suppress("DEPRECATION")
                        stopForeground(false)
                    }
                }
            }
        })

        // create notification channel
        createNotificationChannel()
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(chan)
        }
    }

    private fun startForegroundNotification() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Playing")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(createContentIntent())
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Service started
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Not a bound service in this basic implementation
        return null
    }

    override fun onDestroy() {
        serviceScope.launch {
            try {
                sessionManager.release()
            } catch (_: Throwable) { }
            try {
                player.release()
            } catch (_: Throwable) { }
            serviceScope.cancel()
        }
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "music_playback_channel"
        private const val NOTIFICATION_ID = 4123
    }
}

