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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchRequests(riderId: Long?) {
        if (riderId == null) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAllReqByRiderId(riderId)
                if (response.isSuccessful) {
                    val requests = response.body() ?: emptyList()
                    _rideRequestStates.value = requests.map { RideRequestUIState(it) }
                    requests.forEach { fetchRideStatus(it.rideId) }
                }
            } catch (e: Exception) {
                Log.e("RideRequestVM", "Error fetching requests", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchRideStatus(rideId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getRideByRideId(rideId)
                if (response.isSuccessful) {
                    updateStatusForRide(rideId, response.body()?.status)
                }
            } catch (e: Exception) {
                Log.e("RideRequestVM", "Error fetching ride status", e)
            }
        }
    }

    fun acceptRide(request: RideRequest) = updateRideStatus(request, "ACCEPTED")

    fun rejectRide(request: RideRequest) = updateRideStatus(request, "REJECTED")

    fun startRide(request: RideRequest, onSuccess: () -> Unit) {
        updateLoading(request.id!!, true)
        viewModelScope.launch {
            try {
                val startResponse = RetrofitClient.instance.startRideAndNotifyPassenger(request)
                if (startResponse.isSuccessful) {
                    val updateResponse = RetrofitClient.instance.updateRideRequestStatus(
                        RideRequestStatusUpdateDTO(request.id, "Active")
                    )
                    if (updateResponse.isSuccessful) {
                        updateRequestStatus(request.id, "Active")
                        onSuccess()
                    }
                }
            } catch (e: Exception) {
                Log.e("RideRequestVM", "Error starting ride", e)
            } finally {
                updateLoading(request.id, false)
            }
        }
    }

    private fun updateRideStatus(request: RideRequest, status: String) {
        updateLoading(request.id!!, true)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateRideRequestStatus(
                    RideRequestStatusUpdateDTO(request.id, status)
                )
                if (response.isSuccessful) {
                    updateRequestStatus(request.id, status)
                }
            } finally {
                updateLoading(request.id, false)
            }
        }
    }

    private fun updateStatusForRide(rideId: String, status: String?) {
        _rideRequestStates.value = _rideRequestStates.value.map {
            if (it.rideRequest.rideId == rideId) it.copy(rideStatus = status) else it
        }
    }

    private fun updateRequestStatus(requestId: Long, status: String) {
        _rideRequestStates.value = _rideRequestStates.value.map {
            if (it.rideRequest.id == requestId) {
                it.copy(rideRequest = it.rideRequest.copy(requestStatus = status))
            } else it
        }
    }

    private fun updateLoading(requestId: Long, loading: Boolean) {
        _rideRequestStates.value = _rideRequestStates.value.map {
            if (it.rideRequest.id == requestId) it.copy(isLoading = loading) else it
        }
    }
}

data class RideRequestUIState(
    val rideRequest: RideRequest,
    val rideStatus: String? = null,
    val isLoading: Boolean = false
)


