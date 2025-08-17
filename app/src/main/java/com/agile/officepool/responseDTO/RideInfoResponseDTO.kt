package com.agile.officepool.responseDTO

import java.time.LocalDate
import java.time.LocalTime

data class RideInfoResponseDTO(
        val id: Long,
        val source: String,
        val destination: String,
        val sourceLat: Double,
        val sourceLng: Double,
        val destinationLat: Double,
        val destinationLng: Double,
        val route: String,
        val status: String,
        val rideDate: String,
        val rideStartTime: String,
        val availableSeats: Int,
        val rider: UserDTO
)



