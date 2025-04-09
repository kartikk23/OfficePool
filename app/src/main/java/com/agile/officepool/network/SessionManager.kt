package com.agile.officepool.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RIDER_MODE = "rider_mode"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ID = "userId"
        private const val KEY_AUTH_TOKEN = "authToken"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_PHONE = "phone"
        private const val KEY_USER_COMPANY = "companyName"
        private const val KEY_USER_LINKEDIN_ID = "linkedinId"
        
        

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

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun setUserId(userId : Long){
        prefs.edit().putString(KEY_USER_ID, userId.toString()).apply()
    }

    fun setAuthToken(authToken : String){
        prefs.edit().putString(KEY_AUTH_TOKEN, authToken).apply()
    }

    fun getUsername(): String?{
        return prefs.getString(KEY_USERNAME, "")
    }

    fun getUserPhone(): String?{
        return prefs.getString(KEY_USER_PHONE, "")
    }

    fun setUsername(username:String){
        prefs.edit().putString(KEY_USERNAME,username).apply()
    }

    fun setUserEmail(useremail: String) {
        prefs.edit().putString(KEY_USER_EMAIL,useremail).apply()
    }

    fun setUserPhone(phone: String) {
        prefs.edit().putString(KEY_USER_PHONE,phone).apply()
    }

    fun setCompanyName(companyName: String) {
        prefs.edit().putString(KEY_USER_COMPANY,companyName).apply()
    }

    fun setLinkedInId(linkedinId: String) {
        prefs.edit().putString(KEY_USER_LINKEDIN_ID,linkedinId).apply()
    }

    fun getCompanyName(): String? {
        return prefs.getString(KEY_USER_COMPANY, "")
    }

    fun getLinkedInId(): String? {
        return prefs.getString(KEY_USER_LINKEDIN_ID, "")
    }
}