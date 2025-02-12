package com.example.dblinktest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.Pager
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.dblinktest.ui.theme.DbLinkTestTheme
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    lateinit var viewmodel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewmodel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(this@MainActivity) as T
            }
        })[MainViewModel::class]

        setContent {
            var showUnreadOnly: Boolean by remember { mutableStateOf(false) }
            val remoteChat by viewmodel.remoteChats.collectAsState(emptyList())

            DbLinkTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Data(
                        modifier = Modifier.padding(innerPadding),
                        remoteList = remoteChat,
                        localList = viewmodel.getPager(showUnreadOnly),
                        showUnreadOnly = showUnreadOnly,
                        showAll = {
                            showUnreadOnly = false
                        },
                        showUnread = {
                            showUnreadOnly = true
                        }
                    )
                }
            }
        }
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Data(
    modifier: Modifier = Modifier,
    remoteList: List<RemoteChat>,
    localList: Pager<Int, LocalChat>,
    showUnreadOnly: Boolean,
    showAll: () -> Unit,
    showUnread: () -> Unit
) {
    val localPaging = localList.flow.collectAsLazyPagingItems()

    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            stickyHeader {
                Text(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    text = "Remote data (${remoteList.size})"
                )
            }
            items(remoteList) { chat ->
                ChatItem(
                    modifier = Modifier.fillMaxWidth(),
                    chat = chat
                )
            }
        }

        Column(
            modifier = Modifier.weight(.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = showAll) {
                Text(text = "Show all")
            }

            Button(onClick = showUnread) {
                Text(text = "Show Unread")
            }
        }


        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            stickyHeader {
                Text(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                    text = "Remote data (${localPaging.itemCount})"
                )
            }
                items(localPaging.itemCount, localPaging.itemKey { it.uid }) { index ->
                    val chat = localPaging[index]

                    // Add missing items
                    if (chat != null && index > 0 && showUnreadOnly.not()) {
                        val prevItem = localPaging.peek(index - 1)
                        val diff = chat.uid.minus(prevItem?.uid ?: 0).plus(1).absoluteValue
                        repeat(diff) { missIndex ->
                            ChatItem(
                                modifier = Modifier.fillMaxWidth(),
                                chat = LocalChat(
                                    uid = missIndex,
                                    timeStamp = 0,
                                    content = "Chat #${chat.uid.minus(missIndex - diff)} is Missed",
                                    isRead = false,
                                    nextTimeStamp = 0
                                ),
                                background = Color.Red
                            )
                        }
                    }

                    chat?.let {
                        ChatItem(
                            modifier = Modifier.fillMaxWidth(),
                            chat = chat
                        )
                    }
                }
        }
    }
}


@Composable
fun ChatItem(
    modifier: Modifier = Modifier,
    chat: Chat,
    background: Color? = null
) {
    ElevatedCard(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = background ?: if (chat.isRead) Color.DarkGray else Color.Gray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = chat.content ?: "")
            Text(text = "time: ${chat.timeStamp}")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DbLinkTestTheme {
        ("Android")
    }
}