package com.agile.officepool.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.responseDTO.PageResponse
import com.agile.officepool.responseDTO.RideInfoResponseDTO
import kotlinx.coroutines.launch

class StartupViewModel : ViewModel() {

    val recentRides = mutableStateOf<List<RideInfoResponseDTO>>(emptyList())
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
                // Fixed page=0 and size=4 (or whatever number you want)
                val response = RetrofitClient.instance.getRecentRides(passengerId = passengerId.toString(), page = 0, size = 2)
                if (response.isSuccessful) {
                    val pageResponse: PageResponse<RideInfoResponseDTO>? = response.body()

                    if (pageResponse != null) {
                        // We only need the content
                        recentRides.value = pageResponse.content
                        hasFetchedRecentRides = true
                    } else {
                        errorMessage.value = "Empty response from server."
                    }
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
