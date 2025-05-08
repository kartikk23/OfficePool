package com.agile.officepool.helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.agile.officepool.model.RideInfo
import com.agile.officepool.model.RideRequest
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object RideHelperFunctions {

    fun fetchAvailableRides(onRidesFetched: (List<RideInfo>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllRides()
                val rides = response.body() ?: emptyList()
                withContext(Dispatchers.Main) {
                    onRidesFetched(rides)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Log.d("RIDE_REQUEST", "💥 Exception: ${e.localizedMessage}")
                    onRidesFetched(emptyList()) // Return empty list on failure
                }
            }
        }
    }

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

    fun isFutureRide(rideDate: String, rideTime: String): Boolean {
        return try {
            val rideDateTime = LocalDateTime.of(
                LocalDate.parse(rideDate),
                LocalTime.parse(rideTime)
            )
            rideDateTime.isAfter(LocalDateTime.now())
        } catch (e: Exception) {
            false
        }
    }

    fun filterNearbyRides(rides: List<RideInfo>, sourceLat: Double, sourceLng: Double, destinationLat: Double, destinationLng: Double): List<RideInfo> {
        return rides.filter { ride ->
            val srcDistance = calculateDistance(sourceLat, sourceLng, ride.sourceLat, ride.sourceLng)
            val destDistance = calculateDistance(destinationLat, destinationLng, ride.destinationLat, ride.destinationLng)
            srcDistance <= 2.0 && destDistance <= 2.0 // Show only rides within 2 km
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

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Radius of Earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c // Distance in km
    }
}