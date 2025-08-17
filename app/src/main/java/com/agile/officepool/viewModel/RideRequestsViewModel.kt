package com.agile.officepool.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agile.officepool.model.ReqResponseDTO
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.responseDTO.RideReqResponseDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RideRequestsViewModel : ViewModel() {

    private val _rideRequestStates = MutableStateFlow<List<RideRequestUIState>>(emptyList())
    val rideRequestStates: StateFlow<List<RideRequestUIState>> = _rideRequestStates

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    var currentPage = 0
        private set
    var hasMoreData = true
        private set

    fun fetchRequests(riderId: Long?,reset: Boolean = false) {
        if (riderId == null) return

        if (reset) {
            currentPage = 0
            hasMoreData = true
            _rideRequestStates.value = emptyList()
            _isLoading.value = true
        } else {
            if (_isLoadingMore.value || !hasMoreData) return
            _isLoadingMore.value = true
        }
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAllReqByRiderId(currentPage,10,riderId)
                if (response.isSuccessful) {
                    val requests = response.body()?.content?: emptyList()

                    if (requests.isEmpty()) {
                        hasMoreData = false
                    } else {
                        // Update UI state in one atomic operation
                        val mappedRequests = requests.map { RideRequestUIState(rideRequest = it) }
                        _rideRequestStates.value += mappedRequests

                        // Batch update ride statuses
                        requests.forEach { request ->
                            updateStatusForRide(request.ride.id.toString(), request.ride.status)
                        }

                        currentPage++
                        hasMoreData = !(response.body()?.last ?: true) // Spring Data "last"
                    }

                }
            } catch (e: Exception) {
                Log.e("RideRequestVM", "Error fetching requests", e)
            } finally {
                _isLoading.value = false
                _isLoadingMore.value = false
            }
        }
    }

    fun acceptRide(request: RideReqResponseDTO) = updateRideStatus(request, "ACCEPTED",
        "${request.ride.rider.name.split(" ")[0]} accepted your request",
        "Ride confirmed to ${request.ride.destination}"
    )

    fun rejectRide(request: RideReqResponseDTO) = updateRideStatus(request, "REJECTED",
        "${request.ride.rider.name.split(" ")[0]} rejected your request",
        "Your ride to ${request.ride.destination} was declined."
    )

    fun startRide(request: RideReqResponseDTO, onSuccess: () -> Unit) {
        updateLoading(request.id!!, true)
        viewModelScope.launch {
            try {
                val startResponse = RetrofitClient.instance.startRideAndNotifyPassenger(request)
                if (startResponse.isSuccessful) {
                    val updateResponse = RetrofitClient.instance.updateRideRequestStatus(RideRequestStatusUpdateDTO(request.id, "Active"))
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

    private fun updateRideStatus(request: RideReqResponseDTO, status: String, notificationTitle: String, notificationMsg: String, ) {
        updateLoading(request.id!!, true)
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.updateRideRequestStatus(
                    RideRequestStatusUpdateDTO(request.id, status)
                )
                if (response.isSuccessful) {
                    updateRequestStatus(request.id, status)
                    // ✅ Send FCM Notification
                    try {
                        val notifyResponse = RetrofitClient.instance.sendNotificationToPassenger(
                            ReqResponseDTO(
                                title = notificationTitle,
                                msg = notificationMsg,
                                passengerFcmToken = request.passenger.fcmToken
                            )
                        )
                        if (!notifyResponse.isSuccessful) {
                            Log.e("FCM", "Notification failed. Code: ${notifyResponse.code()}, Body: ${notifyResponse.errorBody()?.string()}")
                        }
                    } catch (e: Exception) {
                        Log.e("FCM", "Error sending notification", e)
                    }
                }
            } finally {
                updateLoading(request.id, false)
            }
        }
    }

    private fun updateStatusForRide(rideId: String, status: String?) {
        _rideRequestStates.value = _rideRequestStates.value.map {
            if (it.rideRequest.ride.id.toString() == rideId) it.copy(rideStatus = status) else it
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
    val rideRequest: RideReqResponseDTO,
    val rideStatus: String? = null,
    val isLoading: Boolean = false
)


