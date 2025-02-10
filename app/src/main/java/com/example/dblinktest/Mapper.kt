package com.example.dblinktest

fun RemoteChat.toLocal(nextTimeStamp: Long? = null) = LocalChat(
    uid = uid,
    timeStamp = timeStamp,
    content = content,
    isRead = isRead,
    nextTimeStamp = nextTimeStamp
)