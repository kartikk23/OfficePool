package com.agile.officepool.ViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.launch

class StartupViewModel : ViewModel() {

    val recentRides = mutableStateOf<List<RideInfo>>(emptyList())
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun fetchRecentRides(passengerId: Long) {
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRecentRides(passengerId.toString())
                if (response.isSuccessful) {
                    recentRides.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Failed with code: ${response.code()}"
                    Log.e("RecentRides", "HTTP error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                errorMessage.value = "Error: ${e.localizedMessage ?: "Unknown error"}"
                Log.e("RecentRides", "Exception: ${e.message}", e)
            } finally {
                isLoading.value = false
            }
        }
    }
}
