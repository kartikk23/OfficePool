package com.agile.officepool.ViewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.model.RideRequest
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RideRequestsViewModel : ViewModel() {

    private val _rideRequestStates = MutableStateFlow<List<RideRequestUIState>>(emptyList())
    val rideRequestStates: StateFlow<List<RideRequestUIState>> = _rideRequestStates

    fun loadRideRequests(requests: List<RideRequest>) {
        _rideRequestStates.value = requests.map { RideRequestUIState(it) }
        requests.forEach { fetchRideStatus(it.rideId) }

    }

    private fun fetchRideStatus(rideId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRideByRideId(rideId)
                if (response.isSuccessful) {
                    updateStatusForRide(rideId, response.body()?.status)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Ride status fetch failed", e)
            }
        }
    }

    fun acceptRide(request: RideRequest) {
        updateLoading(request.id!!, true)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateRideRequestStatus(
                    RideRequestStatusUpdateDTO(request.id, "ACCEPTED")
                )
                if (response.isSuccessful) {
                    updateRequestStatus(request.id, "ACCEPTED")
                }
            } finally {
                updateLoading(request.id, false)
            }
        }
    }

    fun rejectRide(request: RideRequest) {
        updateLoading(request.id!!, true)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateRideRequestStatus(
                    RideRequestStatusUpdateDTO(request.id, "REJECTED")
                )
                if (response.isSuccessful) {
                    updateRequestStatus(request.id, "REJECTED")
                }
            } finally {
                updateLoading(request.id, false)
            }
        }
    }

    fun startRide(request: RideRequest, onSuccess: () -> Unit) {
        updateLoading(request.id!!, true)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.startRideAndNotifyPassenger(request)
                if (response.isSuccessful) {
                    val updateStatusResponse = RetrofitClient.instance.updateRideRequestStatus(
                        RideRequestStatusUpdateDTO(request.id, "ACTIVE")
                    )
                    if (updateStatusResponse.isSuccessful) {
                        updateRequestStatus(request.id, "ACTIVE")
                        onSuccess()
                    }
                }
            } finally {
                updateLoading(request.id, false)
            }
        }
    }

    private fun updateStatusForRide(rideId: String, status: String?) {
        _rideRequestStates.value = _rideRequestStates.value.map {
            if (it.rideRequest.rideId == rideId) {
                it.copy(rideStatus = status)
            } else it
        }
    }



    private fun updateRequestStatus(requestId: Long, status: String) {
        _rideRequestStates.value = _rideRequestStates.value.map {
            if (it.rideRequest.id == requestId) {
                val updatedRequest = it.rideRequest.copy(requestStatus = status)
                it.copy(rideRequest = updatedRequest)
            } else it
        }
    }


    private fun updateLoading(requestId: Long, loading: Boolean) {
        _rideRequestStates.value = _rideRequestStates.value.map {
            if (it.rideRequest.id == requestId) {
                it.copy(isLoading = loading)
            } else it
        }
    }

}

data class RideRequestUIState(
    val rideRequest: RideRequest,
    val rideStatus: String? = null,
    val isLoading: Boolean = false
)

