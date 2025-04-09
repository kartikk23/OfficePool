package com.agile.officepool.rider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RideRequestScreen(rideId: String?) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Incoming Ride Request", fontSize = 24.sp)
        Text("Ride ID: $rideId")
        Button(onClick = { /* accept */ }) { Text("Accept") }
        Button(onClick = { /* reject */ }) { Text("Reject") }
    }
}
