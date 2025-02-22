package com.agile.officepool.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

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
