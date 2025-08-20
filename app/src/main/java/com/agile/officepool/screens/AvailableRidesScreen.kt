package com.agile.officepool.screens


import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.agile.officepool.network.SessionManager
import com.agile.officepool.composableUIScreens.AvailableRidesContent
import com.agile.officepool.viewModel.AvailableRidesViewModel

@Composable
fun AvailableRidesScreen(
    navController: NavController,
    sourceLat: Double,
    sourceLng: Double,
    destinationLat: Double,
    destinationLng: Double,
    viewModel: AvailableRidesViewModel = run {
        val context = LocalContext.current
        val sessionManager = remember { SessionManager(context) }
        val passengerId = sessionManager.getUserId() ?: ""
        remember { AvailableRidesViewModel(passengerId, sourceLat, sourceLng, destinationLat, destinationLng) }
    }
) {
    val state = viewModel.uiState
    val listState = rememberLazyListState()

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.loadMore()
    }

    // Pagination trigger
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                if (lastVisible != null && lastVisible >= state.rides.size - 2) {
                    viewModel.loadMore()
                }
            }
    }

    AvailableRidesContent(
        navController = navController,
        state = state,
        listState = listState,
        viewModel = viewModel
    )
}