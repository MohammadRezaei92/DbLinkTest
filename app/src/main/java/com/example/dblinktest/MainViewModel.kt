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

@OptIn(ExperimentalPagingApi::class)
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
        insertToRemotePeriodically()
    }

    val remoteChats = db.remoteChatDao().getAll()

    private val allPager by lazy {
        Pager(
            PagingConfig(PAGE_SIZE, initialLoadSize = PAGE_SIZE),
            remoteMediator = ChatMediator(showUnreadOnly = false, db.localChatDao(), db.remoteChatDao())
        ) {
            println("Fetch all from local")
            db.localChatDao().getAll()
        }
    }

    private val unreadPager by lazy {
        Pager(
            PagingConfig(PAGE_SIZE, initialLoadSize = PAGE_SIZE),
            remoteMediator = ChatMediator(showUnreadOnly = true, db.localChatDao(), db.remoteChatDao())
        ) {
            println("Fetch Unread from local")
            db.localChatDao().getUnread()
        }
    }

    fun getPager(shoUnreadOnly: Boolean) =
        if (shoUnreadOnly) unreadPager else allPager

    private suspend fun insertDummyData() {
        val count = db.remoteChatDao().getCount()
        if (count != 0) return
        val list = mutableListOf<RemoteChat>()
        repeat(1000) { loop ->
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

    private fun insertToRemotePeriodically() {
        viewModelScope.launch {
            while (true) {
                delay(10000)
                val id = db.remoteChatDao().getCount()
                db.remoteChatDao().insertAll(
                    listOf(
                        RemoteChat(
                            uid = id,
                            timeStamp = System.currentTimeMillis(),
                            "Chat #$id",
                            isRead = Random().nextBoolean()
                        )
                    )
                )
            }
        }
    }
}