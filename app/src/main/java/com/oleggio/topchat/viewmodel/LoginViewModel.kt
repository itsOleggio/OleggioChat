package com.oleggio.topchat.viewmodel

import com.oleggio.topchat.repository.LoginRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.oleggio.topchat.api.ApiService
import com.oleggio.topchat.api.LoginRequest
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val apiService: ApiService,
) : ViewModel() {

    private var _token = MutableStateFlow<String?>(null)
    val token : StateFlow<String?> get() = _token.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error : StateFlow<String?> get() = _error.asStateFlow()

    fun login(name : String, password : String) {
        viewModelScope.launch {
            try {
                val gson = Gson()
                _token.value = apiService.login(gson.toJson(LoginRequest(name, password)))
                loginRepository.saveAuthToken(_token.value.toString())
                loginRepository.saveUsername(name)
            } catch (e : Exception) {
                _error.value = "An error occurred during login: ${e.message}"
                println(e.message)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}