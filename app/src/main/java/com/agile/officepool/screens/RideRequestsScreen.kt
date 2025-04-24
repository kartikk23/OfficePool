import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agile.officepool.model.RideRequest
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideRequestsScreen(navController: NavController) {
    var requests by remember { mutableStateOf<List<RideRequest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    val loadingRequestIds = remember { mutableStateListOf<Long>() }

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val sessionManager = SessionManager(context)
    val riderId = sessionManager.getUserId()?.toLong()

    suspend fun fetchRequests() {
        try {
            Log.d("RideRequestsScreen", "Fetching ride requests for riderId: $riderId")
            val response = riderId?.let { RetrofitClient.instance.getAllReqByRiderId(it) }

            if (response != null && response.isSuccessful) {
                val body = response.body() ?: emptyList()

                // Sort by requestTime in descending order (newest first)
                requests = body.sortedByDescending { it.requestTime }

                Log.d("RideRequestsScreen", "Ride requests received: ${body.size}")
//                Toast.makeText(context, "Fetched ${body.size} requests", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("RideRequestsScreen", "Failed response: ${response?.code()}")
//                Toast.makeText(context, "Failed to fetch requests", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("RideRequestsScreen", "Error fetching requests: ${e.message}")
            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // ✅ Run every time refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        fetchRequests()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride Requests") },
                actions = {
                    IconButton(onClick = {
                        isLoading = true
                        // Trigger reload
                        refreshTrigger++ // ✅ This will re-trigger the LaunchedEffect
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                requests.isEmpty() -> {
                    Text(
                        "No ride requests yet.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests) { request ->

                            RideRequestCard(
                                request = request,
                                isLoading = loadingRequestIds.contains(request.id),
                                onAccept = { selectedRequest ->
                                    coroutineScope.launch {
                                        val requestId = selectedRequest.id ?: return@launch
                                        loadingRequestIds.add(requestId) // 🔄 Add to loading set

                                        val success = try {
                                            val response = RetrofitClient.instance.updateRequestStatus(
                                                RideRequestStatusUpdateDTO(
                                                    id = requestId,
                                                    requestStatus = "ACCEPTED"
                                                )
                                            )
                                            response.isSuccessful
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            false
                                        }

                                        loadingRequestIds.remove(requestId) // ✅ Remove after done

                                        if (success) {
                                            refreshTrigger++
//                                            Toast.makeText(context, "Accepted ${selectedRequest.passengerName}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onReject = { selectedRequest ->
                                    coroutineScope.launch {
                                        val requestId = selectedRequest.id ?: return@launch
                                        loadingRequestIds.add(requestId) // 🔄 Add to loading set

                                        val success = try {
                                            val response = RetrofitClient.instance.updateRequestStatus(
                                                RideRequestStatusUpdateDTO(
                                                    id = selectedRequest.id ?: return@launch,
                                                    requestStatus = "REJECTED"
                                                )
                                            )
                                            response.isSuccessful
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            false
                                        }

                                        loadingRequestIds.remove(requestId) // ✅ Remove after done

                                        if (success) {
                                            refreshTrigger++ // 🔄 Trigger a refresh
//                                            Toast.makeText(context, "Rejected ${selectedRequest.passengerName}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to reject request",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
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

@Composable
fun RideRequestCard(
    request: RideRequest,
    isLoading: Boolean = false,
    onAccept: (RideRequest) -> Unit = {},
    onReject: (RideRequest) -> Unit = {},
    navController: NavController,
    coroutineScope: CoroutineScope
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Passenger Name
            Text(
                text = request.passengerName.trim(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ride ID
            Text(
                text = "Request for Ride ID: ${request.rideId}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conditional UI based on requestStatus
            when (request.requestStatus.uppercase()) {
                "REQUESTED" -> {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp), // ⬅️ smaller size
                                strokeWidth = 2.dp               // ⬅️ thinner stroke
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { onAccept(request) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accept")
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            OutlinedButton(
                                onClick = { onReject(request) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject")
                            }
                        }
                    }
                }


                "ACCEPTED" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Accepted",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }


                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val response = RetrofitClient.instance.getRideByRideId(request.rideId)
                                        if (response.isSuccessful) {
                                            coroutineScope.launch {
                                                try {
                                                    // Construct RideInfo with updated status
                                                    val updatedRide = response.body()!!.copy(status = "Active") // assuming `response` is RideInfo

                                                    val response2 = RetrofitClient.instance.updateRide(updatedRide)
                                                    if (response2.isSuccessful) {
                                                        Log.d("StartRide", "Ride status updated to Active")
                                                        navController.navigate("liveTrackingMap/${request.rideId}/${request.id}")
                                                    } else {
                                                        Log.e("StartRide", "Failed to update ride: ${response2.code()}")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("StartRide", "Exception while updating ride", e)
                                                }
                                            }

                                        } else {
                                            Log.e("RideRequestScreen", "Failed to fetch ride details: ${response.code()}")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("RideRequestScreen", "Error fetching ride details", e)
                                    }
                                }
                              },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Ride")
                        }
                    }

                }

                "REJECTED" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Rejected",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                    }
                }

                "COMPLETED" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.LightGray.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color.Gray),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "COMPLETED",
                                color = Color.Gray,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                    }
                }


                else -> {
                    Text(
                        text = request.requestStatus.capitalize(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}





