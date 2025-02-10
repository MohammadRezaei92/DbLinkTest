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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.dblinktest.ui.theme.DbLinkTestTheme

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
            var isRead: Boolean? by remember { mutableStateOf(null) }
            val remoteChat by viewmodel.remoteChats.collectAsState(emptyList())
            val localChat = viewmodel.getLocalListPage(isRead)?.flow?.collectAsLazyPagingItems()

            DbLinkTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Data(
                        modifier = Modifier.padding(innerPadding),
                        remoteList = remoteChat,
                        localList = localChat,
                        showAll = {
                            isRead = false
                        },
                        showUnread = {
                            isRead = true
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
    localList: LazyPagingItems<LocalChat>?,
    showAll: () -> Unit,
    showUnread: () -> Unit
) {
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

        localList?.let {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                stickyHeader {
                    Text(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        text = "Remote data (${localList.itemCount})"
                    )
                }

                items(localList.itemCount, localList.itemKey { it.uid }) { index ->
                    val chat = localList[index]
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
}


@Composable
fun ChatItem(modifier: Modifier = Modifier, chat: Chat) {
    ElevatedCard(
        modifier = modifier.padding(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (chat.isRead) Color.Gray else Color.DarkGray
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