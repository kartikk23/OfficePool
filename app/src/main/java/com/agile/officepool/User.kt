package com.agile.officepool

// File: User.kt
data class User(
    val id: String,          // Unique identifier from your database
    val name: String,        // User's name
    val email: String,       // User's email
    val linkedInId: String   // LinkedIn ID of the user
)