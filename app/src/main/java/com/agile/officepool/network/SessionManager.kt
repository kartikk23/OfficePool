package com.agile.OfficePool.utils

import android.content.Context
import android.content.SharedPreferences
import com.agile.officepool.model.User

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_AUTH_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_COMPANY = "user_company"
        private const val KEY_USER_LINKEDIN_ID = "user_linkedin_id"
        private const val HAS_RIDE_STARTED = "has_ride_started"
        private const val KEY_USER_UPI_ID = "user_upi_id"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ✅ Save user session after login or registration
    fun saveUserSession(user: User, token: String) {
        with(prefs.edit()) {
            putString(KEY_USER_ID, user.id.toString())
            putString(KEY_USERNAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_PHONE, user.phone)
            putString(KEY_USER_COMPANY, user.companyName)
            putString(KEY_USER_LINKEDIN_ID, user.linkedInId)
            putString(KEY_USER_UPI_ID, user.upiId)
            putString(KEY_AUTH_TOKEN, token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // ✅ Clear session on logout
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    // ✅ Getter methods
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs.getString(KEY_USERNAME, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, null)
    fun getUserCompany(): String? = prefs.getString(KEY_USER_COMPANY, null)
    fun getUserLinkedInId(): String? = prefs.getString(KEY_USER_LINKEDIN_ID, null)
    fun getUserUpiId(): String? = prefs.getString(KEY_USER_UPI_ID, null)
    fun getJwtToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
    fun setHasRideStarted(boolean: Boolean) {
        prefs.edit().putBoolean(HAS_RIDE_STARTED, boolean).apply()
    }

    fun getHasRideStarted(): Boolean {
        return prefs.getBoolean(HAS_RIDE_STARTED, false)
    }

}
