package com.agile.officepool.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun LiveTrackingForPassenger(navController: NavHostController, rideId: String) {

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        Text(
            text = "Live Tracking for Ride ID: $rideId",
            modifier = Modifier.align(Alignment.Center)

        )
    }

}