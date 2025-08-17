package com.agile.officepool.model

import com.google.gson.annotations.SerializedName

// File: User.kt
data class User(
    @SerializedName("id") val id: String,          // Unique identifier from your database
    @SerializedName("name")val name: String,        // User's name
    @SerializedName("email")val email: String,       // User's email
    @SerializedName("linkedInId")val linkedInId: String,
    @SerializedName("phone")val phone: String,
    @SerializedName("companyName")val companyName: String,
    @SerializedName("upiId")val upiId: String?,
    @SerializedName("fcmToken")val fcmToken: String
)