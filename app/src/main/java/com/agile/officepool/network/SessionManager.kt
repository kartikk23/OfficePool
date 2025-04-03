package com.agile.officepool.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun saveSessionToken(token: String) {
        prefs.edit().putString("SESSION_TOKEN", token).apply()
    }

    fun getSessionToken(): String {
        return prefs.getString("SESSION_TOKEN", "") ?: ""
    }

    fun clearSessionToken() {
        prefs.edit().remove("SESSION_TOKEN").apply()
    }

    fun isUserLoggedIn(): Boolean {
        return getSessionToken().isNotEmpty()
    }

}
