package com.agile.officepool.responseDTO

import java.time.LocalDateTime

data class RideReqResponseDTO (
    val id: Long? = null,
    val ride: RideInfoResponseDTO,
    val passenger: UserDTO,
    val requestStatus: String,
    val requestTime: String,
    val rideFare: Int
)