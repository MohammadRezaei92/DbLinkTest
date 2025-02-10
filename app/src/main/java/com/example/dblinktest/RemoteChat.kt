package com.example.dblinktest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteChat(
    @PrimaryKey override val uid: Int,
    override val timeStamp: Long,
    override val content: String?,
    override val isRead: Boolean,
): Chat