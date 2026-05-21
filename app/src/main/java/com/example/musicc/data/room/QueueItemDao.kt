package com.example.musicc.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueItemDao {

    @Query("SELECT * FROM queue_items WHERE session_id = :sessionId ORDER BY position_in_queue ASC")
    fun observeQueue(sessionId: Long): Flow<List<QueueItemEntity>>

    @Insert
    fun insertAll(items: List<QueueItemEntity>): List<Long>

    @Query("DELETE FROM queue_items WHERE session_id = :sessionId")
    fun clearQueue(sessionId: Long)

    @Transaction
    fun replaceQueue(sessionId: Long, items: List<QueueItemEntity>) {
        clearQueue(sessionId)
        if (items.isNotEmpty()) insertAll(items)
    }
}

