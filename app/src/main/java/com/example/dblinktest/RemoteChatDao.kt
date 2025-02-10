package com.example.dblinktest

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteChatDao {
    @Insert
    suspend fun insertAll(chats: List<RemoteChat>)

    @Query("SELECT * FROM RemoteChat")
    fun getAll(): Flow<List<RemoteChat>>

    @Query("SELECT COUNT(uid) FROM remotechat")
    fun getCount(): Int

    @Query("SELECT * FROM RemoteChat WHERE isRead = :isRead AND timeStamp > :timeStamp LIMIT :limit")
    fun getUpdates(
        timeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
        isRead: Boolean,
    ): List<RemoteChat>

    @Query("SELECT * FROM RemoteChat WHERE isRead = :isRead AND timeStamp BETWEEN :fromTimeStamp AND :toTimeStamp LIMIT :limit")
    fun getNextPages(
        fromTimeStamp: Long?,
        toTimeStamp: Long?,
        limit: Int = MainViewModel.PAGE_SIZE,
        isRead: Boolean,
    ): List<RemoteChat>
}