import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agile.officepool.ViewModel.RideRequestsViewModel
import com.agile.officepool.components.RideRequestCard
import com.agile.officepool.components.RideRequestCardShimmer
import com.agile.officepool.components.TopAppBarWithTitle
import com.agile.officepool.helper.RideRequestHelper.fetchRideRequestsForRider
import com.agile.officepool.helper.RideRequestHelper.onRideReqAccept
import com.agile.officepool.helper.RideRequestHelper.onRideReqReject
import com.agile.officepool.model.RideRequest
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.network.RetrofitClient
import com.agile.OfficePool.utils.SessionManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


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
    val isScrollable by remember {
        derivedStateOf { listState.layoutInfo.totalItemsCount > 0 && listState.layoutInfo.visibleItemsInfo.size < listState.layoutInfo.totalItemsCount }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchRequests(riderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 15.dp)
    ) {
        TopAppBarWithTitle(
            title = "Ride Requests",
            onBackClick = { navController.popBackStack() },
            showTrailingIcon = true,
            trailingIcon = Icons.Default.Refresh,
            onTrailingIconClick = { viewModel.fetchRequests(riderId)},
            scrollBehavior = scrollBehavior
        )

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 5.dp)) {
            when {
                isLoading -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.then(
                            if (isScrollable) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                            else Modifier
                        ), 
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                    ) {
                        items(5) { RideRequestCardShimmer() }  // Show 4 shimmer placeholders
                    }
                }

                rideRequests.isEmpty() -> Text(
                    "No ride requests yet.",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                else -> LazyColumn(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),verticalArrangement = Arrangement.spacedBy(15.dp)) {
                    items(rideRequests) { state ->
                        RideRequestCard(
                            state = state,
                            onAccept = { viewModel.acceptRide(it) },
                            onReject = { viewModel.rejectRide(it) },
                            onStart = { viewModel.startRide(it) {
                                navController.navigate("liveTrackingMap/${it.rideId}/${it.id}")
                            }},
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
















