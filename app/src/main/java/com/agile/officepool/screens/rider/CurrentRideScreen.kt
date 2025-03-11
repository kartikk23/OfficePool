package com.agile.officepool.screens.rider

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
//import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberCameraPositionState


    @Composable
    fun CurrentRideScreen(navController: NavController) {
        var isRideActive by remember { mutableStateOf(false) }
        val startLocation = LatLng(37.7749, -122.4194) // Example location

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Current Ride") })
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.weight(1f),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(startLocation, 12f)
                    }
                ) {
                    Marker(state = rememberMarkerState(position = startLocation), title = "Start Location")
                }

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { isRideActive = !isRideActive }) {
                        Text(if (isRideActive) "Stop Ride" else "Start Ride")
                    }
                    Button(onClick = { /* Handle messaging logic */ }) {
                        Text("Message Passenger")
                    }
                    Button(onClick = {
                        val phoneNumber = "1234567890" // Example phone number
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                        navController.context.startActivity(intent)
                    }) {
                        Text("Call Passenger")
                    }
                }
            }
        }
    }