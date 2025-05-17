package com.agile.officepool.screens


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.composableUIScreens.AvailableRidesContent
import com.agile.officepool.helper.RideHelperFunctions.fetchAvailableRides
import com.agile.officepool.helper.RideRequestHelper.fetchRideRequestsForPassenger
import com.agile.officepool.helper.RideHelperFunctions.filterNearbyRides
import com.agile.officepool.helper.RideHelperFunctions.isFutureRide
import com.agile.officepool.model.RideInfo
import com.agile.officepool.model.RideRequest
import com.agile.officepool.network.SessionManager
import com.agile.officepool.ui.theme.OfficePoolTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableRidesScreen(
    navController: NavController,
    sourceName: String,
    sourceLat: Double,
    sourceLng: Double,
    destinationName: String,
    destinationLat: Double,
    destinationLng: Double
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val passengerId = sessionManager.getUserId() ?: ""
    var availableRides by remember { mutableStateOf<List<RideInfo>>(emptyList()) }
    var rideRequests by remember { mutableStateOf<List<RideRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true

        fetchAvailableRides { rides ->
            val nearbyRides = filterNearbyRides(
                rides, sourceLat, sourceLng, destinationLat, destinationLng
            )
            availableRides = nearbyRides
                .filter { isFutureRide(it.rideDate, it.rideStartTime) }
                .sortedByDescending { it.dateTime }

            isLoading = false
        }

        fetchRideRequestsForPassenger(passengerId) {
            rideRequests = it
        }
    }

    AvailableRidesContent(
        navController = navController,
        isLoading = isLoading,
        availableRides = availableRides,
        rideRequests = rideRequests
    )
}