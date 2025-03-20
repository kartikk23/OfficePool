package com.agile.officepool.model

import java.sql.Timestamp

data class PassengerRideViewModel(
    val sourceLat: Double,
    val sourceLng: Double,
    val source: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val destination: String,
    val rideTime: Timestamp

)
