package com.agile.officepool.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.helper.RideHelperFunctions.fetchRidesWithRequests
import com.agile.officepool.helper.RideRequestHelper.sendRideRequest
import com.agile.officepool.model.RideWithRequestStatus
import kotlinx.coroutines.launch

data class AvailableRidesUiState(
    val rides: List<RideWithRequestStatus> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 0,
    val isRefreshing: Boolean = false,
)

class AvailableRidesViewModel(
    private val passengerId: String,
    private val sourceLat: Double,
    private val sourceLng: Double,
    private val destLat: Double,
    private val destLng: Double
) : ViewModel() {

    companion object {
        private const val TAG = "AvailableRidesVM"
    }

    var uiState by mutableStateOf(AvailableRidesUiState())
        private set

    fun loadMore() {
        if (uiState.isLoading || !uiState.hasMore) return

        viewModelScope.launch {
            Log.d(TAG, "Loading more rides: page=${uiState.page}")
            uiState = uiState.copy(isLoading = true)
            try {
                val (newRides, hasMore) = fetchRidesWithRequests(
                    passengerId = passengerId,
                    page = uiState.page,
                    size = 10,
                    sourceLat = sourceLat,
                    sourceLng = sourceLng,
                    destLat = destLat,
                    destLng = destLng
                )

                Log.d(TAG, "Fetched ${newRides.size} rides, hasMore=$hasMore")

                uiState = uiState.copy(
                    rides = uiState.rides + newRides,
                    page = uiState.page + 1,
                    hasMore = hasMore,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading rides", e)
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun sendRideReq(rideId: Long) {
        viewModelScope.launch {
            try{
                val response = sendRideRequest(passengerId, rideId.toInt())
                if (response!=null) {
                    uiState = uiState.copy(
                        rides = uiState.rides.map { current ->
                            if (current.ride.id == rideId) {
                                current.copy(requestStatus = response.requestStatus)
                            } else current
                        }
                    )
                } else {
                    Log.w(TAG, "Ride request failed for rideId=$rideId")
                }

            }catch (e: Exception)
            {
                Log.e(TAG, "Error sending ride request for rideId=$rideId", e)
            }
        }
    }

    fun refreshRides() {
        viewModelScope.launch {
            Log.d(TAG, "Refreshing rides...")
            uiState = uiState.copy(isRefreshing = true)

            try {
                // Reset state
                val (newRides, hasMore) = fetchRidesWithRequests(
                    passengerId = passengerId,
                    page = 0,
                    size = 10,
                    sourceLat = sourceLat,
                    sourceLng = sourceLng,
                    destLat = destLat,
                    destLng = destLng
                )

                uiState = uiState.copy(
                    rides = newRides,
                    page = 1,  // reset page count
                    hasMore = hasMore,
                    isRefreshing = false,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing rides", e)
                uiState = uiState.copy(isRefreshing = false)
            }
        }
    }

}
