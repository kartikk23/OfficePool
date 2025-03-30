package com.agile.officepool.model

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)
