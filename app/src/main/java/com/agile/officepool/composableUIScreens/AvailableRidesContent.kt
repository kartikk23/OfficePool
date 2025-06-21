package com.agile.officepool.composableUIScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agile.officepool.components.RideCard
import com.agile.officepool.components.TopAppBarWithTitle
import com.agile.officepool.model.RideInfo
import com.agile.officepool.model.RideRequest

@Composable
fun AvailableRidesContent(
    navController: NavController,
    isLoading: Boolean,
    availableRides: List<RideInfo>,
    rideRequests: List<RideRequest>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 15.dp)
    ) {
        TopAppBarWithTitle(
            title = "Available Rides",
            onBackClick = { navController.popBackStack() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                availableRides.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No nearby rides available")
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(availableRides) { ride ->
                        val request = rideRequests.find { it.rideId == ride.rideId.toString() }
                        RideCard(ride = ride, rideRequest = request)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewAvailableRidesContent() {
    // Sample dummy data
    val mockRides = listOf(
        RideInfo(
            rideId = 1,
            riderId = "r123",
            source = "Downtown",
            destination = "Office Park",
            sourceLat = 0.0,
            sourceLng = 0.0,
            destinationLat = 0.0,
            destinationLng = 0.0,
            route = "road",
            status = "Yet to start",
            availableSeats = "3",
            rideStartTime = "09:00:00",
            rideDate = "2025-05-09",
            dateTime = "2025-05-09T09:00:00"
        ),
        RideInfo(
            rideId = 2,
            riderId = "r123",
            source = "City Center",
            destination = "Tech Hub",
            sourceLat = 0.0,
            sourceLng = 0.0,
            destinationLat = 0.0,
            destinationLng = 0.0,
            route = "road",
            status = "Yet to start",
            availableSeats = "3",
            rideStartTime = "09:30:00",
            rideDate = "2025-05-09",
            dateTime = "2025-05-09T09:00:00"
        ),
    )

    val mockRequests = listOf(
        RideRequest(
            rideId = "1",
            passengerId = "p001",
            passengerName = "John",
            riderId = "r123",
            requestStatus = "REQUESTED"
        )
    )

    AvailableRidesContent(
        navController = NavController(LocalContext.current),
        isLoading = false,
        availableRides = mockRides,
        rideRequests = mockRequests
    )
}