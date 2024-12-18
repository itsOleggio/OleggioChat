package com.oleggio.topchat.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.oleggio.topchat.api.Chat
import com.oleggio.topchat.room.ChatDao
import com.oleggio.topchat.room.ChatEntity
import com.oleggio.topchat.api.Message
import javax.inject.Inject

class ChatRepository @Inject constructor(private val dao : ChatDao) {
    private val _selectedChat = MutableStateFlow("")
    val selectedChat: StateFlow<String> get() = _selectedChat.asStateFlow()

    private var _newMessages = MutableStateFlow<List<Message>>(mutableListOf())
    val newMessages: StateFlow<List<Message>> get() = _newMessages.asStateFlow()

    fun setSelectedChat(newData: String) {
        _selectedChat.value = newData
    }

    fun addIncomingMessage(msg : Message) {
        _newMessages.value += msg
    }

    fun popIncomingMessage() : Message {
        val message = _newMessages.value.first()
        _newMessages.value = _newMessages.value.drop(1)
        return message
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

    suspend fun deleteChatsFromDb() {
        return dao.deleteAllChats()
    }
}