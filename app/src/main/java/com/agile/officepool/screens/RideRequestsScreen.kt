package com.agile.officepool.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agile.officepool.viewModel.RideRequestsViewModel
import com.agile.officepool.components.RideRequestCard
import com.agile.officepool.components.RideRequestCardShimmer
import com.agile.officepool.components.TopAppBarWithTitle
import com.agile.officepool.network.SessionManager


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRequestsScreen(
    navController: NavController,
    viewModel: RideRequestsViewModel
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val riderId = sessionManager.getUserId()?.toLong()
    val rideRequests by viewModel.rideRequestStates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val listState = rememberLazyListState()

    // Initial fetch
    LaunchedEffect(riderId) {
        if (riderId != null) {
            viewModel.fetchRequests(riderId, true)
        }
    }

    // Scroll listener for pagination
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val totalItems = rideRequests.size
                val shouldLoadMore = lastVisibleIndex != null &&
                        lastVisibleIndex >= totalItems - 5 && // trigger early
                        viewModel.hasMoreData &&
                        !viewModel.isLoadingMore.value &&
                        !viewModel.isLoading.value

                if (shouldLoadMore) {
                    viewModel.fetchRequests(riderId,false)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBarWithTitle(
            title = "Ride Requests",
            onBackClick = { navController.popBackStack() },
            showTrailingIcon = true,
            trailingIcon = Icons.Default.Refresh,
            onTrailingIconClick = { viewModel.fetchRequests(riderId,true)},
            scrollBehavior = scrollBehavior
        )

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            when {
                isLoading -> {
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                    ) {
                        items(5) { RideRequestCardShimmer() }  // Show 5 shimmer placeholders
                    }
                }

                rideRequests.isEmpty() -> Text(
                    "No ride requests yet.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                else -> LazyColumn(state = listState) {
                    items(rideRequests) { state ->
                        RideRequestCard(
                            state = state,
                            onAccept = { viewModel.acceptRide(it) },
                            onReject = { viewModel.rejectRide(it) },
                            onStart = { viewModel.startRide(it) {
                                navController.navigate("liveTrackingMap/${it.ride.id}/${it.id}")
                            }},
                            navController = navController
                        )
                    }
                    // 🔄 Inline loading placeholder while fetching more
                    if (viewModel.isLoadingMore.value) {
                        items(2) { RideRequestCardShimmer() }  // or use CircularProgressIndicator
                    }
                }

            }
        }
    }
}
















