package com.agile.officepool.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.User
import com.agile.officepool.helper.LinkedInAuthHelper
import com.agile.officepool.helper.LinkedInUserInfo
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    private val linkedInAuthHelper = LinkedInAuthHelper()

    // Function to get the authorization URL
    fun getAuthorizationUrl(): String {
        return linkedInAuthHelper.getAuthorizationUrl()
    }

    // Function to handle LinkedIn login
    fun loginWithLinkedIn(code: String, onSuccess: (LinkedInUserInfo) -> Unit, onError: (Throwable) -> Unit) {
        viewModelScope.launch {
            try {
                // Step 1: Get the access token
                val accessToken = linkedInAuthHelper.getAccessToken(code)
                Log.d("LinkedInLogin","ACCESS TOKEN ${accessToken}")

                // Step 2: Get user info using the access token
                val userInfo = linkedInAuthHelper.getUserInfo(accessToken)
                Log.d("LinkedInLogin","INFO ${userInfo}")

                // Step 3: Trigger the success callback
                val response = RetrofitClient.instance.verifyLinkedIn(accessToken)
                viewModelScope.launch { // Launch a coroutine

//                    if (response.isSuccessful) {
//                        Log.d("LinkedInLogin","RES ${response}")
//                        _user.postValue(response.body())
//
//                    }
                }
                onSuccess(userInfo)
            } catch (e: Exception) {
                // Step 4: Handle errors
                onError(e)
            }
        }
    }
}