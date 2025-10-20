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
        val radiusKm = 3000.0
        val ridesDeferred = async(Dispatchers.IO) {
            RetrofitClient.instance.getAllNearbyRides("dateTime", "desc", passengerId.toLong(),page, size, sourceLat, sourceLng, destLat, destLng,
                radiusKm,radiusKm).body()
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
}