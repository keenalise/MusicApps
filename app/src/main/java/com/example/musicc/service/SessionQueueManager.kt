package com.example.musicc.service

import android.net.Uri
import com.example.musicc.data.repo.ISessionRepository
import com.example.musicc.domain.QueueItem
import com.example.musicc.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * SessionQueueManager bridges between the Music Library (Song objects from MediaStore)
 * and the Session system (QueueItem objects in Room DB).
 *
 * Use this class to:
 * 1. Add songs from the music library to a session's queue
 * 2. Replace a session's queue with a new set of songs
 * 3. Ensure proper URI validation for playback
 */
class SessionQueueManager(
    private val sessionRepository: ISessionRepository
) {

    /**
     * Convert a Song (from MediaStore) to a QueueItem for session storage.
     * Validates the URI to ensure it's safe for playback.
     */
    private fun songToQueueItem(song: Song, positionInQueue: Int): QueueItem? {
        return try {
            val uri = song.uri ?: return null
            val uriString = uri.toString()

            // Extract and validate URI scheme from string representation
            val scheme = when {
                uriString.startsWith("content://") -> "content"
                uriString.startsWith("http://") -> "http"
                uriString.startsWith("https://") -> "https"
                else -> null
            }

            // Only allow whitelisted schemes
            if (scheme == null) {
                return null
            }

            QueueItem(
                mediaId = song.id,
                uri = uriString,
                title = song.title,
                artist = song.artist,
                album = song.album,
                durationMs = song.durationMs,
                positionInQueue = positionInQueue
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Add songs to an existing session's queue.
     * The songs are appended to the end of the current queue.
     *
     * @param sessionId The ID of the session to add songs to
     * @param songs The list of Song objects to add
     * @throws Exception if the session doesn't exist or database operation fails
     */
    suspend fun addSongsToSession(sessionId: Long, songs: List<Song>) {
        withContext(Dispatchers.IO) {
            try {
                val session = sessionRepository.sessionFlow(sessionId).first()
                    ?: throw Exception("Session not found: $sessionId")

                val newQueueItems = songs.mapIndexedNotNull { index, song ->
                    songToQueueItem(song, session.queue.size + index)
                }

                if (newQueueItems.isNotEmpty()) {
                    val allItems = session.queue + newQueueItems
                    sessionRepository.replaceQueue(sessionId, allItems)
                }
            } catch (e: Exception) {
                throw Exception("Failed to add songs to session: ${e.message}", e)
            }
        }
    }

    /**
     * Replace a session's entire queue with a new set of songs.
     * This clears any existing queue items and starts fresh.
     *
     * @param sessionId The ID of the session
     * @param songs The new list of Song objects to populate the queue
     * @throws Exception if the session doesn't exist or database operation fails
     */
    suspend fun replaceSessionQueue(sessionId: Long, songs: List<Song>) {
        withContext(Dispatchers.IO) {
            try {
                val queueItems = songs.mapIndexedNotNull { index, song ->
                    songToQueueItem(song, index)
                }

                if (queueItems.isNotEmpty()) {
                    sessionRepository.replaceQueue(sessionId, queueItems)
                } else {
                    // Clear queue if no valid songs
                    sessionRepository.replaceQueue(sessionId, emptyList())
                }
            } catch (e: Exception) {
                throw Exception("Failed to replace session queue: ${e.message}", e)
            }
        }
    }

    /**
     * Create a new session with an initial queue of songs.
     *
     * @param sessionTitle The title/name of the session
     * @param songs The initial songs to add to the session's queue
     * @return The newly created session ID
     */
    suspend fun createSessionWithSongs(sessionTitle: String, songs: List<Song>): Long {
        return withContext(Dispatchers.IO) {
            try {
                val queueItems = songs.mapIndexedNotNull { index, song ->
                    songToQueueItem(song, index)
                }

                sessionRepository.createSession(sessionTitle, queueItems)
            } catch (e: Exception) {
                throw Exception("Failed to create session with songs: ${e.message}", e)
            }
        }
    }

    /**
     * Get the playable URI for a song in a session.
     * This validates the URI before returning it.
     *
     * @param sessionId The session ID
     * @param queueItemIndex The index in the queue
     * @return The validated Uri object, or null if invalid
     */
    suspend fun getPlayableUri(sessionId: Long, queueItemIndex: Int): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val session = sessionRepository.sessionFlow(sessionId).first()
                    ?: return@withContext null

                if (queueItemIndex < 0 || queueItemIndex >= session.queue.size) {
                    return@withContext null
                }

                val item = session.queue[queueItemIndex]

                // Validate scheme from URI string
                val isValidScheme = item.uri.startsWith("content://") ||
                        item.uri.startsWith("http://") ||
                        item.uri.startsWith("https://")

                if (isValidScheme) Uri.parse(item.uri) else null
            } catch (_: Exception) {
                null
            }
        }
    }
}


