package com.agile.officepool.screens


import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.agile.OfficePool.utils.SessionManager
import com.agile.officepool.composableUIScreens.AvailableRidesContent
import com.agile.officepool.helper.RideHelperFunctions.fetchAvailableRides
import com.agile.officepool.helper.RideRequestHelper.fetchRideRequestsForPassenger
import com.agile.officepool.helper.RideHelperFunctions.filterNearbyRides
import com.agile.officepool.helper.RideHelperFunctions.isFutureRide
import com.agile.officepool.responseDTO.RideInfoResponseDTO
import com.agile.officepool.responseDTO.RideReqResponseDTO


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

    var availableRides by remember { mutableStateOf<List<RideInfoResponseDTO>>(emptyList()) }
    var rideRequests by remember { mutableStateOf<List<RideReqResponseDTO>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    var hasMoreRides by remember { mutableStateOf(true) }
    var hasMoreRequests by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    fun loadMore() {
        if (isLoading || (!hasMoreRides && !hasMoreRequests)) return
        isLoading = true

        // Fetch rides for current page
        fetchAvailableRides("dateTime", "desc", currentPage, 10) { rides ->
            val nearbyRides = filterNearbyRides(
                rides, sourceLat, sourceLng, destinationLat, destinationLng
            ).filter { isFutureRide(it.rideDate.toString(), it.rideStartTime.toString()) }

            if (rides.isEmpty()) hasMoreRides = false
            availableRides = availableRides + nearbyRides

            // Fetch requests for the same page
            fetchRideRequestsForPassenger(passengerId, currentPage, 10,
                onRequestsFetched = { requests, hasMore ->
                    rideRequests = rideRequests + requests
                    hasMoreRequests = hasMore
                    if (hasMoreRides || hasMoreRequests) currentPage++
                    isLoading = false
                },
                onError = {
                    isLoading = false
                }
            )
        }
    }

    // First load
    LaunchedEffect(Unit) {
        loadMore()
    }

    // Trigger pagination when near bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleItem ->
                if (lastVisibleItem != null && lastVisibleItem >= availableRides.size - 2) {
                    loadMore()
                }
            }
    }

    AvailableRidesContent(
        navController = navController,
        isLoading = isLoading,
        availableRides = availableRides,
        rideRequests = rideRequests,
        listState = listState
    )
}