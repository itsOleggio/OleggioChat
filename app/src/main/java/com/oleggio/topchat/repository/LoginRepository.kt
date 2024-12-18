package com.oleggio.topchat.repository

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class LoginRepository @Inject constructor(
    context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun getAuthToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    fun clearAuthToken() {
        sharedPreferences.edit().remove("auth_token").apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    fun saveUsername(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
    }

    fun clearUsername() {
        sharedPreferences.edit().remove("user_name").apply()
    }
}