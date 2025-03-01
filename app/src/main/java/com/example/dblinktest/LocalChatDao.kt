package com.example.dblinktest

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocalChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<LocalChat>)

    @Query("SELECT * FROM LocalChat ORDER BY timeStamp DESC")
    fun getAll(): PagingSource<Int,LocalChat>

    @Query("SELECT * FROM LocalChat WHERE isRead = 1 ORDER BY timeStamp DESC")
    fun getUnread(): PagingSource<Int,LocalChat>

    @Query("SELECT * FROM localchat ORDER BY timeStamp DESC LIMIT 1")
    fun getFirstItem(): LocalChat?

    @Query("SELECT * FROM localchat ORDER BY timeStamp ASC LIMIT 1")
    fun getLastItem(): LocalChat?

    @Query("SELECT * FROM localchat WHERE isRead = 1 ORDER BY timeStamp DESC LIMIT 1")
    fun getFirstUnreadItem(): LocalChat?

    @Query("SELECT * FROM localchat WHERE isRead = 1 ORDER BY timeStamp ASC LIMIT 1")
    fun getLastUnreadItem(): LocalChat?
}