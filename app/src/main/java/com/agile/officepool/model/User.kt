package com.agile.officepool.model

// File: User.kt
data class User(
    val id: String,          // Unique identifier from your database
    val name: String,        // User's name
    val email: String,       // User's email
    val linkedInId: String,
    val phone: String,
    val companyName: String

)
