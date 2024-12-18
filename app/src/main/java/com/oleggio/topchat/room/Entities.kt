package com.oleggio.topchat.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Int,
    val from: String,
    val to: String,
    val text: String,
    val image: String?,
    val time: Long
)
