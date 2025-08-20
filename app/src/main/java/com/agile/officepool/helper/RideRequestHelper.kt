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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

object RideRequestHelper {

//    suspend fun sendRideRequestSuspend(
//        passengerId: String,
//        rideId: Int,
//    ): Boolean = suspendCancellableCoroutine { continuation ->
//        sendRideRequest(passengerId, rideId) { success ->
//            if (continuation.isActive) {
//                continuation.resume(success) {}
//            }
//        }
//    }

    suspend fun sendRideRequest(
        passengerId: String,
        rideId: Int
    ): RideReqResponseDTO? = withContext(Dispatchers.IO) {
        try {
            val request = RideRequest(
                ride = RideIdDTO(id = rideId.toLong()),
                passenger = UserIdDTO(id = passengerId.toLong()),
                requestStatus = "REQUESTED"
            )

            val response = RetrofitClient.instance.addRideReq(request)
            if (!response.isSuccessful) return@withContext null

            val body = response.body() ?: return@withContext null
            val ride = body.ride
            val passenger = body.passenger
            val riderFcmToken = ride.rider.fcmToken

            // fire-and-forget notification, no need to block UI state update
            riderFcmToken.let {
                try {
                    val riderFcmDTO = RiderFCMDTO(
                        RideDetailDTO(ride.id, ride.source, ride.destination),
                        passenger,
                        it
                    )
                    RetrofitClient.instance.sendNotificationToRider(riderFcmDTO)
                } catch (e: Exception) {
                    Log.w("RIDE_REQUEST", "Notification failed: ${e.localizedMessage}")
                }
            }

            return@withContext body // ✅ directly return server DTO
        } catch (e: Exception) {
            Log.e("RIDE_REQUEST", "💥 Exception: ${e.localizedMessage}")
            null
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