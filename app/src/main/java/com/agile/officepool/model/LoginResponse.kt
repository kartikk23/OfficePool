package com.agile.officepool.model

data class LoginResponse(
    val user: User,
    val tokenOrMessage: String,
)