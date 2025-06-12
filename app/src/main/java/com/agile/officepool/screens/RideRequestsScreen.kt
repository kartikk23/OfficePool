import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agile.officepool.components.RideRequestCard
import com.agile.officepool.components.TopAppBarWithTitle
import com.agile.officepool.helper.RideRequestHelper.fetchRideRequestsForRider
import com.agile.officepool.helper.RideRequestHelper.onRideReqAccept
import com.agile.officepool.helper.RideRequestHelper.onRideReqReject
import com.agile.officepool.model.RideRequest
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRequestsScreen(navController: NavController) {
    var requests by remember { mutableStateOf<List<RideRequest>>(emptyList()) }
    var isLoading1 by remember { mutableStateOf(true) }
    var refreshTrigger = remember { mutableIntStateOf(0) }
    val loadingRequestIds = remember { mutableStateListOf<Long>() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val riderId = sessionManager.getUserId()?.toLong()

    // ✅ Run every time refreshTrigger changes
    LaunchedEffect(refreshTrigger.intValue) {
        fetchRideRequestsForRider(
            riderId = riderId,
            onResult = { result ->
                requests = result
            },
            onError = { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                isLoading1 = false
            },
            onComplete = {
                isLoading1 = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 15.dp)
    ) {
        TopAppBarWithTitle(
            title = "Ride Requests",
            onBackClick = { navController.popBackStack() },
            showTrailingIcon = true,
            trailingIcon = Icons.Default.Refresh,
            onTrailingIconClick = {
                isLoading1 = true
                refreshTrigger.intValue++
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                isLoading1 -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                requests.isEmpty() -> {
                    Text(
                        "No ride requests yet.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                        contentPadding = PaddingValues(top = 2.dp, bottom = 10.dp)
                    ) {
                        items(requests) { request ->

                            RideRequestCard(
                                request = request,
                                isLoading1 = loadingRequestIds.contains(request.id),
                                onAccept = { selectedRequest ->
                                    coroutineScope.launch {
                                        onRideReqAccept(
                                            selectedRequest = selectedRequest,
                                            loadingRequestIds = loadingRequestIds,
                                            refreshTrigger = refreshTrigger,
                                            context = context
                                        )
                                    }

                                },
                                onReject = { selectedRequest ->
                                    coroutineScope.launch {
                                        onRideReqReject(
                                            selectedRequest = selectedRequest,
                                            loadingRequestIds = loadingRequestIds,
                                            refreshTrigger = refreshTrigger,
                                            context = context
                                        )
                                    }
                                },
                                navController = navController,
                                coroutineScope = coroutineScope

                            )
                        }
                    }
                }
            }
        }
    }

}














