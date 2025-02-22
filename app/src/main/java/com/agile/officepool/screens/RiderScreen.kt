package com.agile.officepool.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController



@Composable
fun RiderScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rider Mode", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Navigate to Add Ride Screen */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Ride")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Your Active Rides",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn {
                items(dummyRides) { ride ->
                    RideItem(ride)
                }
            }
        }
    }
}

@Composable
fun RideItem(ride: Ride) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* Handle Ride Click */ },
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("From: ${ride.startLocation}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("To: ${ride.endLocation}", fontSize = 16.sp, color = Color.Gray)
            Text("Seats Available: ${ride.seatsAvailable}", fontSize = 14.sp, color = Color.Blue)
            Text("Departure: ${ride.departureTime}", fontSize = 14.sp, color = Color.Green)
        }
    }
}

// Sample Ride Data
data class Ride(
    val startLocation: String,
    val endLocation: String,
    val seatsAvailable: Int,
    val departureTime: String
)

val dummyRides = listOf(
    Ride("Downtown", "Tech Park", 2, "9:00 AM"),
    Ride("City Center", "Airport", 3, "10:30 AM"),
    Ride("Metro Station", "University", 1, "8:15 AM")
)
