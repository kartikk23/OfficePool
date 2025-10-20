package com.agile.officepool.responseDTO

import com.agile.officepool.model.GeoPointDTO

data class RideInfoResponseDTO(
        val id: Long,
        val source: String,
        val destination: String,
        val sourceLocation: GeoPointDTO,
        val destinationLocation: GeoPointDTO,
        val route: String,
        val status: String,
        val rideDate: String,
        val rideStartTime: String,
        val availableSeats: Int,
        val rider: UserDTO
)



