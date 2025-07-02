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

    // Flag to check if recent rides were already fetched
    private var hasFetchedRecentRides = false

    fun fetchRecentRides(passengerId: Long) {

        // Prevent re-fetch if already done
        if (hasFetchedRecentRides) return

        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRecentRides(passengerId.toString())
                if (response.isSuccessful) {
                    recentRides.value = response.body() ?: emptyList()
                    hasFetchedRecentRides = true  // Mark as fetched
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

    // Optional: if you ever want to refetch manually
    fun resetRecentRidesFetchFlag() {
        hasFetchedRecentRides = false
    }
}
