package com.example.dblinktest

interface Chat {
    val uid: Int
    val timeStamp: Long
    val content: String?
    val isRead: Boolean
}