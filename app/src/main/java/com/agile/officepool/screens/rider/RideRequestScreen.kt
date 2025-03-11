package com.agile.officepool.rider

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRequestScreen(navController: NavController, viewModel: RideRequestViewModel = viewModel()) {
    val rideRequests = remember { viewModel.rideRequests }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ride Requests") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(rideRequests) { request ->
                RideRequestCard(request) {
                    navController.navigate("currentRide")
                }
            }
        }
    }
}

data class RideRequest(val passengerName: String, val source: String, val destination: String, val phoneNumber: String)

class RideRequestViewModel : ViewModel() {
    val rideRequests = listOf(
        RideRequest("John Doe", "Route A", "Route B", "1234567890"),
        RideRequest("Jane Smith", "Route A", "Route B", "9876543210"),
        RideRequest("Alex Johnson", "Route A", "Route B", "5678901234")
    )
}

@Composable
fun RideRequestCard(request: RideRequest, onAccept: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Passenger: ${request.passengerName}")
            Text(text = "From: ${request.source} To: ${request.destination}")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onAccept) {
                    Text("Accept")
                }
                Button(onClick = { /* Handle Reject */ }) {
                    Text("Reject")
                }
            }
        }
    }
}
