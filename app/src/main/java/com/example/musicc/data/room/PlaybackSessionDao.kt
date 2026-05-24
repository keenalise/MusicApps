package com.example.musicc.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackSessionDao {

    @Query("SELECT * FROM playback_sessions ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<PlaybackSessionEntity>>

    @Query("SELECT * FROM playback_sessions WHERE id = :sessionId LIMIT 1")
    fun observeById(sessionId: Long): Flow<PlaybackSessionEntity?>

    @Transaction
    @Query("SELECT * FROM playback_sessions WHERE id = :sessionId LIMIT 1")
    fun observeSessionWithQueue(sessionId: Long): Flow<PlaybackSessionWithQueue?>

    @Insert
    fun insert(session: PlaybackSessionEntity): Long

    @Update
    fun update(session: PlaybackSessionEntity): Int

    @Query("DELETE FROM playback_sessions WHERE id = :sessionId")
    fun delete(sessionId: Long): Int

    @Query("UPDATE playback_sessions SET is_active = 0 WHERE (:exceptId IS NULL) OR id != :exceptId")
    fun clearActiveFlagsExcept(exceptId: Long?): Int
}
