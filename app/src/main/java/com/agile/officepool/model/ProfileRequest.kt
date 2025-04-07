package com.agile.officepool.model

data class ProfileRequest(
    val email: String,
    val name : String,
    val phone: String,
    val companyName: String,
    val linkedInId: String
)