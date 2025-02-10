package com.example.dblinktest

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocalChat::class, RemoteChat::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun localChatDao(): LocalChatDao
    abstract fun remoteChatDao(): RemoteChatDao
}

