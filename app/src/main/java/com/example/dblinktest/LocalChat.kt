package com.example.dblinktest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalChat(
    @PrimaryKey override val uid: Int,
    override val timeStamp: Long,
    override val content: String?,
    override val isRead: Boolean,
    val nextTimeStamp: Long?
): Chat