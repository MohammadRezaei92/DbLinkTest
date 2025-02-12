package com.example.dblinktest

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator

@OptIn(ExperimentalPagingApi::class)
class ChatMediator(
    private val showUnreadOnly: Boolean,
    private val localDataSource: LocalChatDao,
    private val remoteDataSource: RemoteChatDao
) : RemoteMediator<Int, LocalChat>() {

    // Always refresh list to get new data
   /* override suspend fun initialize(): InitializeAction {
        val firstItem = localDataSource.getFirstItem(isRead = showUnreadOnly.not())
        return if (firstItem == null)
            InitializeAction.LAUNCH_INITIAL_REFRESH
        else
            InitializeAction.SKIP_INITIAL_REFRESH
    }*/

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, LocalChat>
    ): MediatorResult {

        fun getLastItem() =
            if(showUnreadOnly) localDataSource.getLastUnreadItem() else localDataSource.getLastItem()
        fun getFirstItem() =
            if (showUnreadOnly) localDataSource.getFirstUnreadItem() else localDataSource.getFirstItem()

        val remoteData = when (loadType) {
            LoadType.REFRESH -> {
                // Send time stamp of first item on refresh
                val firstItem = getFirstItem()
                val key = firstItem?.timeStamp ?: 0
                println("Refresh data from:${firstItem?.uid}")
                if (showUnreadOnly)
                    remoteDataSource.getUnreadUpdates(timeStamp = key)
                else
                    remoteDataSource.getUpdates(timeStamp = key)
            }

            LoadType.PREPEND -> {
                println("Prepend data")
                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                println("Append data")
                // When user scroll get the last item
                // If last item is linked get data for range
                val lastItem = getLastItem()
                if (lastItem?.nextTimeStamp != null) {
                    val from = lastItem.timeStamp
                    val to = lastItem.nextTimeStamp
                    println("Fetch data from:$from to:$to")
                    if (showUnreadOnly)
                        remoteDataSource.getUnreadNextPages(
                            fromTimeStamp = from,
                            toTimeStamp = to
                        )
                    else
                        remoteDataSource.getNextPages(
                            fromTimeStamp = from,
                            toTimeStamp = to
                        )
                } else { // If last item doesn't have link we reach to end of list
                    println("Fetch next page after:${lastItem?.timeStamp}")
                    if (showUnreadOnly)
                        remoteDataSource.getUnreadNextPages(timeStamp = lastItem?.timeStamp)
                    else
                        remoteDataSource.getNextPages(timeStamp = lastItem?.timeStamp)
                }
            }
        }


        val mappedData = remoteData.map { it.toLocal() }
        if (showUnreadOnly) {
            val lastItem = getLastItem()
            // Add time stamp of first remote item to the last item in db
            if (loadType == LoadType.REFRESH) {
                val firstRemoteItem = remoteData.firstOrNull()
                if (lastItem != null && firstRemoteItem != null) {
                    localDataSource.insertAll(
                        listOf(lastItem.copy(nextTimeStamp = firstRemoteItem.timeStamp))
                    )
                    println("Set time stamp of last:$lastItem to ${firstRemoteItem.uid}")
                }
            }

            if (loadType == LoadType.APPEND) {
                // Link remote data together
                val linkedList = mappedData.linkItems(loadType = loadType, lastItem = lastItem)
                // Remove link from last local item
                lastItem?.let {
                    localDataSource.insertAll(
                        listOf(lastItem.copy(nextTimeStamp = null))
                    )
                }
                println("Items after link ${listOf(lastItem?.copy(nextTimeStamp = null)) + linkedList}")
                localDataSource.insertAll(linkedList)
            } else
                localDataSource.insertAll(mappedData)
        } else
            localDataSource.insertAll(mappedData)

        println("Insert ${mappedData.size} new items to db")

        val reachEnd = loadType == LoadType.APPEND && remoteData.size < MainViewModel.PAGE_SIZE

        return MediatorResult.Success(
            endOfPaginationReached = reachEnd
        )
    }

    private fun List<LocalChat>.linkItems(
        loadType: LoadType,
        lastItem: LocalChat?
    ): List<LocalChat> {
        return when (loadType) {
            LoadType.REFRESH -> {
                updateNextTimeStamps()
            }

            LoadType.PREPEND -> {
                this
            }

            LoadType.APPEND -> {
                if (isEmpty())
                    return emptyList()

                lastItem?.let {
                    updateLastItemTimeStamp(it)
                } ?: this
            }
        }
    }

    private fun List<LocalChat>.updateNextTimeStamps(): List<LocalChat> {
        return mapIndexed { index, currentItem ->
            val nextItem = getOrNull(index + 1)
            currentItem.copy(nextTimeStamp = nextItem?.timeStamp)
        }
    }

    private fun List<LocalChat>.updateLastItemTimeStamp(lastItem: LocalChat): List<LocalChat> {
        return mapIndexed { index, currentItem ->
            if (index == lastIndex)
                currentItem.copy(nextTimeStamp = lastItem.nextTimeStamp)
            else
                currentItem
        }
    }
}