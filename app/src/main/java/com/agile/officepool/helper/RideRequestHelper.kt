package com.agile.officepool.helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation.NavController
import com.agile.officepool.model.ReqResponseDTO
import com.agile.officepool.model.RideDetailDTO
import com.agile.officepool.model.RideIdDTO
import com.agile.officepool.model.RideRequest
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.model.RiderFCMDTO
import com.agile.officepool.model.UserIdDTO
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.responseDTO.RideReqResponseDTO
import com.agile.officepool.responseDTO.UserDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object RideRequestHelper {

    fun sendRideRequest(
        passengerId: String,
        rideId:Int,
        context: Context,
        onResult: (Boolean) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = RideRequest(
                    ride= RideIdDTO(id = rideId.toLong()),
                    passenger = UserIdDTO(id = passengerId.toLong()),
                    requestStatus = "REQUESTED")
                Log.d("RIDE_REQUEST", "📤 Sending ride request to backend... $request")

                val response = RetrofitClient.instance.addRideReq(request);

                if (response.isSuccessful) {

                    Log.d("RIDE_REQUEST", "✅ Ride request saved: ${response.body()}")

                    // Show immediate feedback to user
                    withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Ride request saved. Sending notification...", Toast.LENGTH_SHORT).show()
                    }
                    val ride = response.body()?.ride!!
                    val passenger = response.body()?.passenger!!
                    val riderFcmToken = response.body()?.ride?.rider?.fcmToken!!
                    val riderFcmDTO  = RiderFCMDTO(
                        RideDetailDTO(ride.id,ride.source,ride.destination),
                        passenger,
                        riderFcmToken
                    )
                    // 🔔 Trigger FCM Notification to Rider
                    val notifyResponse = RetrofitClient.instance.sendNotificationToRider(riderFcmDTO)
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


    fun fetchRideRequestsForPassenger(
        passengerId: String,
        page: Int,
        size: Int,
        onRequestsFetched: (List<RideReqResponseDTO>,Boolean) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllReqByPassengerId(page,size,passengerId.toLong())
                if (response.isSuccessful) {
                    val requests = response.body()?.content ?: emptyList() // Page<T> format → use `.content`
                    val hasMore = !(response.body()?.last ?: true) // `last` is from Spring Data Page object
                    withContext(Dispatchers.Main) {
                        onRequestsFetched(requests,hasMore)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onRequestsFetched(emptyList(),false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onRequestsFetched(emptyList(),false)
                    onError(e)
                }
            }
        }
    }
}