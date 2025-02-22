package com.agile.officepool.model

import com.google.gson.annotations.SerializedName

data class RideInfo(
    @SerializedName("riderId") val riderId: String,
    @SerializedName("source") val source: String,
    @SerializedName("destination") val destination: String,
    @SerializedName("route") val route: String,
    @SerializedName("status") val status: String,
    @SerializedName("availableSeats") val availableSeats: String,
    @SerializedName("dateTime") val dateTime: String = ""

)