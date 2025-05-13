package com.agile.officepool.helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.agile.officepool.model.ReqResponseDTO
import com.agile.officepool.model.RideRequest
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object RideRequestHelper {

    fun sendRideRequest(
        passengerId: String,
        passengerName: String,
        rideId:Int,
        riderId: String,
        context: Context,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = RideRequest(
                    rideId=rideId.toString(),
                    passengerId = passengerId,
                    passengerName = passengerName,
                    riderId = riderId,
                    requestStatus = "REQUESTED")
                Log.d("RIDE_REQUEST", "📤 Sending ride request to backend... $request")

                val response = RetrofitClient.instance.addRideReq(request);


                if (response.isSuccessful && response.body()?.success == true) {

                    Log.d("RIDE_REQUEST", "✅ Ride request saved: ${response.body()}")

                    // Show immediate feedback to user
                    withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Ride request saved. Sending notification...", Toast.LENGTH_SHORT).show()
                    }

                    // 🔔 Trigger FCM Notification to Rider
                    val notifyResponse = RetrofitClient.instance.sendNotificationToRider(request)
                    val res = notifyResponse.body()
                    Log.d("RIDE_REQUEST", "📨 Notification response body: $res")
                    Log.d("RIDE_REQUEST", "📨 Notification response status: ${notifyResponse.code()}")




                    withContext(Dispatchers.Main) {
                        if (res != null) {
                            if (notifyResponse.isSuccessful && res.success){
//                            Toast.makeText(context, "✅ Ride request notification sent!", Toast.LENGTH_LONG).show()
                                onResult(true)
                            } else {
//                            Toast.makeText(context, "⚠\uFE0F FCM failed: ${res.message}", Toast.LENGTH_LONG).show()
                                onResult(false)
                            }
                        }
                    }
                } else {
                    Log.e("RIDE_REQUEST", "❌ Ride request failed: ${response.errorBody()?.string()}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "❌ Ride request failed!", Toast.LENGTH_LONG).show()
                        onResult(false)
                    }
                }
            } catch (e: Exception) {
                Log.e("RIDE_REQUEST", "💥 Exception: ${e.localizedMessage}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "⚠️ Error sending ride request", Toast.LENGTH_LONG).show()
                    onResult(false)
                }
            }
        }
    }

    suspend fun fetchRideRequestsForRider(
        riderId: Long?,
        onResult: (List<RideRequest>) -> Unit,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        try {
            Log.d("RideRequestsScreen", "Fetching ride requests for riderId: $riderId")
            val response = riderId?.let { RetrofitClient.instance.getAllReqByRiderId(it) }

            if (response != null && response.isSuccessful) {
                val body = response.body() ?: emptyList()

                // Sort by requestTime in descending order (newest first)
                onResult(body.sortedByDescending { it.requestTime })

                Log.d("RideRequestsScreen", "Ride requests received: ${body.size}")
            } else {
                Log.e("RideRequestsScreen", "Failed response: ${response?.code()}")
                onError("Failed to fetch ride requests.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RideRequestsScreen", "Error fetching requests: ${e.message}")
            onError(e.localizedMessage ?: "Unknown error")
        } finally {
            onComplete()
        }
    }


    suspend fun onRideReqAccept(
        selectedRequest: RideRequest,
        loadingRequestIds: SnapshotStateList<Long>,
        refreshTrigger: MutableState<Int>,
        context: Context
    ) {
        val requestId = selectedRequest.id ?: return
        loadingRequestIds.add(requestId)

        val success = try {
            val response = RetrofitClient.instance.updateRequestStatus(
                RideRequestStatusUpdateDTO(
                    id = requestId,
                    requestStatus = "ACCEPTED"
                )
            )
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

        loadingRequestIds.remove(requestId)

        if (success) {
            refreshTrigger.value++

            // ✅ Send FCM Notification
            try {
                val notifyResponse = RetrofitClient.instance.sendNotificationToPassenger(
                    ReqResponseDTO(
                        title = "Ride Accepted",
                        msg = "Your ride has been accepted. Get ready to roll!",
                        passengerId = selectedRequest.passengerId ?: ""
                    )
                )
                if (!notifyResponse.isSuccessful) {
                    Log.e("FCM", "Notification failed")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error sending notification", e)
            }

        } else {
            Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun onRideReqReject(
        selectedRequest: RideRequest,
        loadingRequestIds: SnapshotStateList<Long>,
        refreshTrigger: MutableState<Int>,
        context: Context
    ){
        val requestId = selectedRequest.id ?: return
        loadingRequestIds.add(requestId) // 🔄 Add to loading set

        val success = try {
            val response =
                RetrofitClient.instance.updateRequestStatus(
                    RideRequestStatusUpdateDTO(
                        id = selectedRequest.id,
                        requestStatus = "REJECTED"
                    )
                )
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

        loadingRequestIds.remove(requestId) // ✅ Remove after done

        if (success) {
            refreshTrigger.value++ // 🔄 Trigger a refresh
//        Toast.makeText(context, "Rejected ${selectedRequest.passengerName}", Toast.LENGTH_SHORT).show()
            // ✅ Send FCM Notification
            try {
                val notifyResponse = RetrofitClient.instance.sendNotificationToPassenger(
                    ReqResponseDTO(
                        title = "Ride Rejected",
                        msg = "Your ride request has been rejected.",
                        passengerId = selectedRequest.passengerId ?: ""
                    )
                )
                if (!notifyResponse.isSuccessful) {
                    Log.e("FCM", "Notification failed")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error sending notification", e)
            }
        } else {
            Toast.makeText(
                context,
                "Failed to reject request",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun fetchRideRequestsForPassenger(
        passengerId: String,
        onRequestsFetched: (List<RideRequest>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllReqByPassengerId(passengerId.toLong())
                val requests = response.body() ?: emptyList()
                withContext(Dispatchers.Main) {
                    onRequestsFetched(requests)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onRequestsFetched(emptyList())
                }
            }
        }
    }
}