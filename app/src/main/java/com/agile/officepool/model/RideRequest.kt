package com.agile.officepool.model

data class RideRequest(
    val id: Long? = null,
    val rideId: String,
    val passengerId: String,
    val passengerName: String,
    val riderId: String,
    val requestStatus: String,
    val requestTime: String? = null
)