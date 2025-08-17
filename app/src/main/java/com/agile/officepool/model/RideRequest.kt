package com.agile.officepool.model

import com.google.gson.annotations.SerializedName

data class RideRequest(
    val id: Long? = null,
    val ride: RideIdDTO,
    val passenger: UserIdDTO,
    val requestStatus: String,
    val requestTime: String? = null,
)

data class RideIdDTO(
    @SerializedName("id") val id: Long
)