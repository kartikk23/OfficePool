package com.agile.officepool.responseDTO

data class UserDTO(
    val id: Long,
    val name: String,
    val email: String,
    val linkedInId: String,
    val phone: String,
    val companyName: String,
    val upiId: String,
    val fcmToken: String
)
