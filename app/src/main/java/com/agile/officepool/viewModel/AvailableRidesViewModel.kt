package com.agile.officepool.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.helper.RideHelperFunctions.fetchRidesWithRequests
import com.agile.officepool.helper.RideRequestHelper.sendRideRequest
import com.agile.officepool.model.RideWithRequestStatus
import com.agile.officepool.responseDTO.RideReqResponseDTO
import kotlinx.coroutines.launch

data class AvailableRidesUiState(
    val rides: List<RideWithRequestStatus> = emptyList(),
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 0
)

class AvailableRidesViewModel(
    private val passengerId: String,
    private val sourceLat: Double,
    private val sourceLng: Double,
    private val destLat: Double,
    private val destLng: Double
) : ViewModel() {

    var uiState by mutableStateOf(AvailableRidesUiState())
        private set

    fun loadMore() {
        if (uiState.isLoading || !uiState.hasMore) return

        viewModelScope.launch {
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

                uiState = uiState.copy(
                    rides = uiState.rides + newRides,
                    page = uiState.page + 1,
                    hasMore = hasMore,
                    isLoading = false
                )
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    fun sendRideReq(rideId: Long) {
        viewModelScope.launch {
            val success = sendRideRequest(passengerId, rideId.toInt())
            if (success) {
                uiState = uiState.copy(
                    rides = uiState.rides.map { current ->
                        if (current.ride.id == rideId) {
                            current.copy(
                                request = RideReqResponseDTO(
                                    id = -1,
                                    ride = current.ride,
                                    requestStatus = "REQUESTED",
                                    passenger = current.request?.passenger!!, // ✅ null-safe
                                    requestTime = System.currentTimeMillis().toString(),
                                    rideFare = current.request.rideFare
                                )
                            )
                        } else current
                    }
                )
            }
        }
    }
}
