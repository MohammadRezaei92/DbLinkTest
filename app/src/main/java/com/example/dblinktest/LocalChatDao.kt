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

    @Query("SELECT * FROM LocalChat ORDER BY timeStamp")
    fun getAll(): PagingSource<Int,LocalChat>

    @Query("SELECT * FROM LocalChat WHERE isRead = 1 ORDER BY timeStamp")
    fun getUnread(): PagingSource<Int,LocalChat>

    @Query("SELECT * FROM localchat ORDER BY timeStamp LIMIT 1")
    fun getFirstItem(): LocalChat?

    @Query("SELECT * FROM localchat ORDER BY timeStamp DESC LIMIT 1")
    fun getLastItem(): LocalChat?
}