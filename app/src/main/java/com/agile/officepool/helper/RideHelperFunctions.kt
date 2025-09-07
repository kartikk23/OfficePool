package com.agile.officepool.helper

import android.util.Log
import com.agile.officepool.model.RideWithRequestStatus
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.responseDTO.RideInfoResponseDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    suspend fun fetchRidesWithRequests(
        passengerId: String,
        page: Int,
        size: Int,
        sourceLat: Double,
        sourceLng: Double,
        destLat: Double,
        destLng: Double
    ): Pair<List<RideWithRequestStatus>, Boolean> = coroutineScope {
        val ridesDeferred = async(Dispatchers.IO) {
            RetrofitClient.instance.getAllNearbyRides("dateTime", "desc", passengerId.toLong(),page, size, sourceLat, sourceLng, destLat, destLng,
                5000.0,5000.0).body()
        }
        val ridesResponse = ridesDeferred.await()

        val rides = ridesResponse?.content ?: emptyList()

        // Merge request into ride
        val merged = rides.map { ride ->
            RideWithRequestStatus(
                ride = ride.ride,
                requestStatus = ride.requestStatus
            )
        }
        val hasMore = !(ridesResponse?.last ?: true)
        merged to hasMore
    }


    fun fetchAvailableRides(sortByField : String ,order : String , page: Int, size: Int, onRidesFetched: (List<RideInfoResponseDTO>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getAllRides(sortByField,order,page,size)
                val rides = response.body()?.content ?: emptyList()
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

    fun filterNearbyRides(rides: List<RideInfoResponseDTO>, sourceLat: Double, sourceLng: Double, destinationLat: Double, destinationLng: Double): List<RideInfoResponseDTO> {
        return rides.filter { ride ->
            val srcDistance = calculateDistance(sourceLat, sourceLng, ride.sourceLat, ride.sourceLng)
            val destDistance = calculateDistance(destinationLat, destinationLng, ride.destinationLat, ride.destinationLng)
            srcDistance <= 2.0 && destDistance <= 2.0 // Show only rides within 2 km
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