package com.agile.officepool.model

data class RegisterResponse(
    val user: User,
    val token: String,
    val message: String
)