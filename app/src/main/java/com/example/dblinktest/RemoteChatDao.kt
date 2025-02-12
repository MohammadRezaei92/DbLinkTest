package com.example.dblinktest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteChatDao {
    @Insert
    suspend fun insertAll(chats: List<RemoteChat>)

    @Query("SELECT * FROM RemoteChat ORDER BY timeStamp DESC")
    fun getAll(): Flow<List<RemoteChat>>

    @Query("SELECT COUNT(uid) FROM remotechat")
    fun getCount(): Int

    @Query("SELECT * FROM RemoteChat WHERE timeStamp > :timeStamp ORDER BY timeStamp DESC LIMIT :limit")
    fun getUpdates(
        timeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
    ): List<RemoteChat>

    @Query("SELECT * FROM RemoteChat WHERE isRead = 1 AND timeStamp > :timeStamp ORDER BY timeStamp DESC LIMIT :limit")
    fun getUnreadUpdates(
        timeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
    ): List<RemoteChat>

    @Query("SELECT * FROM RemoteChat WHERE timeStamp BETWEEN :fromTimeStamp AND :toTimeStamp LIMIT :limit")
    fun getNextPages(
        fromTimeStamp: Long?,
        toTimeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
    ): List<RemoteChat>

    @Query("SELECT * FROM RemoteChat WHERE isRead = 1 AND timeStamp BETWEEN :fromTimeStamp AND :toTimeStamp LIMIT :limit")
    fun getUnreadNextPages(
        fromTimeStamp: Long?,
        toTimeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
    ): List<RemoteChat>

    @Query("SELECT * FROM RemoteChat WHERE timeStamp < :timeStamp ORDER BY timeStamp DESC LIMIT :limit")
    fun getNextPages(
        timeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
    ): List<RemoteChat>

    @Query("SELECT * FROM RemoteChat WHERE isRead = 1 AND timeStamp < :timeStamp ORDER BY timeStamp DESC LIMIT :limit")
    fun getUnreadNextPages(
        timeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
    ): List<RemoteChat>
}