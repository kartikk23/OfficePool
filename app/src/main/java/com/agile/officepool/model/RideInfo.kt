package com.agile.officepool.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.math.*

data class RideInfo(
    @SerializedName("rideId") val rideId: Int? = null,
    @SerializedName("riderId") val riderId: String,
    @SerializedName("source") val source: String,
    @SerializedName("destination") val destination: String,
    @SerializedName("sourceLat") val sourceLat: Double,
    @SerializedName("sourceLng") val sourceLng: Double,
    @SerializedName("destinationLat") val destinationLat: Double,
    @SerializedName("destinationLng") val destinationLng: Double,
    @SerializedName("route") val route: String,
    @SerializedName("status") val status: String,
    @SerializedName("availableSeats") val availableSeats: String,
    @SerializedName("rideStartTime") val rideStartTime: String,
    @SerializedName("rideDate") val rideDate: String,
    @SerializedName("dateTime") val dateTime: String? = null,

    ){
    fun calculateDistanceInKm(): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val latDistance = Math.toRadians(destinationLat - sourceLat)
        val lonDistance = Math.toRadians(destinationLng - sourceLng)

        val a = sin(latDistance / 2).pow(2.0) +
                cos(Math.toRadians(sourceLat)) * cos(Math.toRadians(destinationLat)) *
                sin(lonDistance / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    fun calculatePrice(): Int {
        val distance = calculateDistanceInKm()
        val pricePerKm = 4.0 // ₹4 per kilometer
        return (distance * pricePerKm).roundToInt()
    }
}