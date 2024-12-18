package com.oleggio.topchat.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChats(chat: List<ChatEntity>)

    @Query("SELECT * FROM chats")
    suspend fun getAllChats(): List<ChatEntity>

    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE `to`=:chatId")
    suspend fun getMessages(chatId : String): List<MessageEntity>

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}