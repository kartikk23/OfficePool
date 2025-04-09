package com.agile.officepool.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RIDER_MODE = "rider_mode"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun setRiderMode(isRider: Boolean) {
        prefs.edit().putBoolean(KEY_RIDER_MODE, isRider).apply()
    }

    fun isRiderMode(): Boolean {
        return prefs.getBoolean(KEY_RIDER_MODE, false)
    }

    fun saveUserSession(email: String) {
        prefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }
    fun saveUserEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }


    fun saveUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
    }

    fun getUserName(): String? {
        return prefs.getString("user_name", null)
    }

    fun saveUserPhone(phone: String) {
        prefs.edit().putString("user_phone", phone).apply()
    }

    fun getUserPhone(): String? {
        return prefs.getString("user_phone", null)
    }

    fun saveCompanyName(companyName: String) {
        prefs.edit().putString("company_name", companyName).apply()
    }

    fun getCompanyName(): String? {
        return prefs.getString("company_name", null)
    }

    fun saveLinkedInId(linkedInId: String) {
        prefs.edit().putString("linkedin_id", linkedInId).apply()
    }

    fun getLinkedInId(): String? {
        return prefs.getString("linkedin_id", null)
    }

}