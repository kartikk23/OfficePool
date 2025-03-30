package com.agile.officepool.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    fun saveSessionToken(token: String) {
        prefs.edit().putString("SESSION_COOKIE", token).apply()
    }

    fun getSessionToken(): String {
        return prefs.getString("SESSION_COOKIE", "") ?: ""
    }

    fun clearSessionToken() {
        prefs.edit().remove("SESSION_COOKIE").apply()
    }
    companion object {
        private const val KEY_RIDER_MODE = "rider_mode"
    }

    fun setRiderMode(isRider: Boolean) {
        prefs.edit().putBoolean(KEY_RIDER_MODE, isRider).apply()
    }

    fun isRiderMode(): Boolean {
        return prefs.getBoolean(KEY_RIDER_MODE, false) // Default is OFF
    }
}
