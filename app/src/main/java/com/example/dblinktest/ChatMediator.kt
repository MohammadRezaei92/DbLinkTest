package com.example.dblinktest

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator

@OptIn(ExperimentalPagingApi::class)
class ChatMediator(
    private val isRead: Boolean,
    private val localDataSource: LocalChatDao,
    private val remoteDataSource: RemoteChatDao
): RemoteMediator<Int, LocalChat>() {

    override suspend fun initialize(): InitializeAction {
        val firstItem = localDataSource.getFirstItem()
        return if (firstItem == null)
            InitializeAction.LAUNCH_INITIAL_REFRESH
        else
            InitializeAction.SKIP_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, LocalChat>): MediatorResult {
       val remoteData = when(loadType) {
            LoadType.REFRESH -> {
                // Send time stamp of first item on refresh
                val firstItem = state.firstItemOrNull()
                val key = firstItem?.timeStamp ?: 0
                println("Refresh data with timestamp:$key")
                remoteDataSource.getUpdates(timeStamp = key, isRead = isRead)
            }
            LoadType.PREPEND -> {
                println("Prepend data")
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                println("Append data")
                // When user scroll get the last item
                val lastItem = state.lastItemOrNull()
                // If last item is linked get data for range
                if (lastItem?.nextTimeStamp != null) {
                    val from = lastItem.timeStamp
                    val to = lastItem.nextTimeStamp
                    // Remove link of last item
                    localDataSource.insertAll(
                        listOf(lastItem.copy(nextTimeStamp = null))
                    )
                    println("Fetch data from:$from to:$to")
                    remoteDataSource.getNextPages(fromTimeStamp = from, toTimeStamp = to, isRead = isRead)
                } else { // If last item doesn't have link we reach to end of list
                    println("Fetch next page")
                    remoteDataSource.getUpdates(timeStamp = lastItem?.timeStamp, isRead = isRead)
                }
            }
        }

        // Add time stamp of first remote item to the last item in db
        val lastLocalItem = localDataSource.getLastItem()
        val firstRemoteItem = remoteData.firstOrNull()
        if(lastLocalItem != null && firstRemoteItem != null) {
            localDataSource.insertAll(
                listOf(lastLocalItem.copy(nextTimeStamp = firstRemoteItem.timeStamp))
            )
            println("Set time stamp of last:$lastLocalItem to ${firstRemoteItem.timeStamp}")
        }

        // Link new items to each other
        localDataSource.insertAll(remoteData.map { it.toLocal() }.linkItems())
        println("Insert ${remoteData.size} new items to db")

        return MediatorResult.Success(
            endOfPaginationReached = remoteData.size < MainViewModel.PAGE_SIZE
        )
    }

    private fun List<LocalChat>.linkItems(): List<LocalChat> {
        val newList = mutableListOf<LocalChat>()
        for(i in indices) {
            getOrNull(i)?.copy(nextTimeStamp = getOrNull(i+1)?.timeStamp)?.let {
                newList.add(it)
            }
        }
        println("Link items: $newList")
        return newList
    }

}