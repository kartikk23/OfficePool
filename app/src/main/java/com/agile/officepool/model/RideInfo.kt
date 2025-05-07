package com.agile.officepool.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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

    )