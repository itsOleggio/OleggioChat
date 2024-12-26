package com.oleggio.topchat.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.oleggio.topchat.api.Chat
import com.oleggio.topchat.room.ChatDao
import com.oleggio.topchat.room.ChatEntity
import javax.inject.Inject

class ChatRepository @Inject constructor(private val dao : ChatDao) {
    private val _selectedChat = MutableStateFlow("")
    val selectedChat: StateFlow<String> get() = _selectedChat.asStateFlow()

    fun setSelectedChat(newData: String) {
        _selectedChat.value = newData
    }

    fun clearSelectedChat() {
        _selectedChat.value = ""
    }

    suspend fun getChatsFromDb() : List<Chat> {
        val chatEntities = dao.getAllChats()
        val chats : MutableList<Chat> = mutableListOf()

        for (i : ChatEntity in chatEntities) {
            val chat = Chat(
                id = i.id,
                name = i.name,
            )
            chats.add(chat)
        }

        return chats
    }

    suspend fun saveChatsToDb(chats : List<Chat>) {
        val chatEntities : MutableList<ChatEntity> = mutableListOf()
        for (i : Chat in chats) {
            val chat = ChatEntity(
                id = i.id,
                name = i.name
            )
            chatEntities.add(chat)
        }
        dao.insertAllChats(chatEntities)
    }
}