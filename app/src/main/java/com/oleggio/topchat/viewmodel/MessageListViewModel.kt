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
import com.oleggio.topchat.api.Message
import com.oleggio.topchat.api.MessageData
import com.oleggio.topchat.api.Text
import com.oleggio.topchat.api.checkConnection
import com.oleggio.topchat.repository.LoginRepository
import com.oleggio.topchat.repository.ChatRepository
import com.oleggio.topchat.repository.MessageRepository
import javax.inject.Inject

@HiltViewModel
class MessageListViewModel @Inject constructor(
    private var chatRepository: ChatRepository,
    private var messageRepository: MessageRepository,
    private var loginRepository: LoginRepository,
    private val apiService: ApiService
) : ViewModel() {
    private var _messageList = MutableStateFlow<List<Message>>(emptyList())
    val messageList: StateFlow<List<Message>> get() = _messageList.asStateFlow()

    val selectedChat: StateFlow<String> get() = chatRepository.selectedChat

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error : StateFlow<String?> get() = _error.asStateFlow()

    fun clearError() {
        _error.value = null
    }

    private var _messageInput = MutableStateFlow("")
    var messageInput: StateFlow<String>
        get() = _messageInput.asStateFlow()
        set(value) {
            _messageInput.value = value.value
        }

    fun onTextChanged(text : String) {
        _messageInput.value = text
    }

    fun clearMessageList() {
        _messageList.value = emptyList()
    }

    fun clearSelected() {
        chatRepository.clearSelectedChat()
    }

    fun cleanUserInput() {
        _messageInput.value = ""
    }

    fun getMessageList(context: Context, lastId : Int = 0) {
        if (_isLoading.value) {
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val messagesList : List<Message>
                if (checkConnection(context)) {
                    messagesList = apiService.getMessages(selectedChat.value, lastKnownId = lastId, reverse = true)
                    messageRepository.insertAllMessages(messagesList)
                } else {
                    messagesList = messageRepository.getChatMessages(selectedChat.value)
                }
                _messageList.value += messagesList
            } catch (e : Exception) {
                _error.value = "An error occured during receiving messages: ${e.message}"
                println(e.message)
            }
        }
        _isLoading.value = false
    }

    fun sendMessage(context: Context) {
        if (!checkConnection(context)) {
            _error.value = "You're in offline mode. Please turn on Internet connection"
            return
        }
        val username = loginRepository.getUsername()
        val token = loginRepository.getAuthToken()
        if (username == null || token == null) {
            _error.value = "Please, authorize first in order to send messages"
            return
        }
        val msg = Message(
            to = selectedChat.value,
            from = username,
            id = 0,
            data = MessageData(
                text = Text(text = messageInput.value),
                image = null
            ),
            time = 0
        )

        try {
            viewModelScope.launch {
                apiService.sendMessage(token, msg)
            }
            _messageList.value = listOf(msg) + _messageList.value
        } catch (e : Exception) {
            _error.value = "An error occured while sending message: ${e.message}"
        }
    }
}

