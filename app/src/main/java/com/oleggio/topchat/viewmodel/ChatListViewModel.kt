package com.oleggio.topchat.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.oleggio.topchat.api.ApiService
import com.oleggio.topchat.api.Chat
import com.oleggio.topchat.api.checkConnection
import com.oleggio.topchat.repository.ChatRepository
import com.oleggio.topchat.repository.LoginRepository
import java.util.LinkedList
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val loginRepository: LoginRepository,
    private val apiService : ApiService,
) : ViewModel() {
    private var _chatList = MutableStateFlow<List<Chat>>(emptyList())
    val chatList: StateFlow<List<Chat>> get() = _chatList.asStateFlow()

    private var _userList = MutableStateFlow<List<Chat>>(emptyList())
    val userList: StateFlow<List<Chat>> get() = _userList.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    var error : StateFlow<String?> get() = _error.asStateFlow()
        set(value) { _error.value = value.value }

    fun clearError() {
        _error.value = null
    }

    fun selectChat(chatID : String) {
        repository.setSelectedChat(chatID)
    }

    fun getChatList(context: Context) {
        viewModelScope.launch {
            try {
                val chatList : List<Chat>
                if (checkConnection(context)) {
                    val channelNames = apiService.getChannels()
                    chatList = LinkedList<Chat>()

                    var k = 1
                    for (i in channelNames) {
                        chatList.add(Chat((k++).toString(), i))
                    }
                    repository.saveChatsToDb(chatList)
                } else {
                    chatList = repository.getChatsFromDb()
                }
                _chatList.value = chatList
            } catch (e : Exception) {
                _error.value = "An error occurred during receiving chats: ${e.message}"
                println(e.message)
            }
        }
    }

    fun getUserList(context: Context) {
        viewModelScope.launch {
            try {
                val chatList : List<Chat>
                if (checkConnection(context)) {
                    val userNames = apiService.getUsers()
                    chatList = LinkedList<Chat>()

                    var k = 1
                    for (i in userNames) {
                        chatList.add(Chat((k++).toString(), i))
                    }
                    repository.saveChatsToDb(chatList)
                } else {
                    chatList = repository.getChatsFromDb()
                }
                _userList.value = chatList
            } catch (e : Exception) {
                _error.value = "An error occurred during receiving chats: ${e.message}"
                println(e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                apiService.logout(loginRepository.getAuthToken() ?: "")
                loginRepository.clearUsername()
                loginRepository.clearAuthToken()
            } catch (e : Exception) {
                println(e.message)
                _error.value = "Failed to logout: ${e.message}"
            }
        }
    }

    fun getUsername() : String? {
        return loginRepository.getUsername()
    }
}

