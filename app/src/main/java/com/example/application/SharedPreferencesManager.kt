package com.example.application

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("tasksaver_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_AUTO_SYNC = "auto_sync_enabled"
    }

    fun saveUserSession(token: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_TOKEN, token)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserToken(): String? = prefs.getString(KEY_USER_TOKEN, null)

    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    fun setAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun isAutoSyncEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SYNC, true)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}