package com.agile.officepool.model

import com.agile.officepool.responseDTO.UserDTO

data class RiderFCMDTO(
    val rideInfo: RideDetailDTO,
    val passenger: UserDTO,
    val riderFCMToken: String,
)

data class RideDetailDTO(
    val rideId: Long,
    val source: String,
    val destination: String,
)
