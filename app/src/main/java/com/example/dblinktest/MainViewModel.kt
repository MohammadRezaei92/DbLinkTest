package com.example.dblinktest

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

class MainViewModel(
    context: Context
) : ViewModel() {
    companion object {
        const val PAGE_SIZE = 20
    }

    private var db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "appdb"
    ).allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()


    init {
        viewModelScope.launch(Dispatchers.IO) { insertDummyData() }
    }

    val remoteChats = db.remoteChatDao().getAll()

    private suspend fun insertDummyData() {
        val count = db.remoteChatDao().getCount()
        if (count != 0) return
        val list = mutableListOf<RemoteChat>()
        repeat(5000) { loop ->
            delay(1)
            list.add(
                RemoteChat(
                    uid = loop,
                    timeStamp = System.currentTimeMillis(),
                    content = "Chat #$loop",
                    isRead = Random().nextBoolean()
                )
            )
        }
        db.remoteChatDao().insertAll(list)
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getLocalListPage(isRead: Boolean?): Pager<Int, LocalChat>? {
        if (isRead == null) return null
        return Pager(
            PagingConfig(PAGE_SIZE, initialLoadSize = PAGE_SIZE),
            remoteMediator = ChatMediator(isRead = isRead, db.localChatDao(), db.remoteChatDao())
        ) {
            println("Fetch from local isread:$isRead")
            if (isRead)
                db.localChatDao().getUnread()
            else
                db.localChatDao().getAll()
        }
    }
}