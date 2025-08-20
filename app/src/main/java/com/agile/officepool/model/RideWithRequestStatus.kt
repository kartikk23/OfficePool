package com.agile.officepool.model

import com.agile.officepool.responseDTO.RideInfoResponseDTO
import com.agile.officepool.responseDTO.RideReqResponseDTO

data class RideWithRequestStatus(
    val ride: RideInfoResponseDTO,
    val request: RideReqResponseDTO? = null
)
