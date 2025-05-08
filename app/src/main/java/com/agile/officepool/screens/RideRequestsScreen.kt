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
    var isLoading1 by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
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
            isLoading1 = false
        }
    }

    // ✅ Run every time refreshTrigger changes
    LaunchedEffect(refreshTrigger) {
        fetchRequests()
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
                // Trigger reload
                refreshTrigger++ // ✅ This will re-trigger the LaunchedEffect}
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(requests) { request ->

                            RideRequestCard(
                                request = request,
                                isLoading1 = loadingRequestIds.contains(request.id),
                                onAccept = { selectedRequest ->
                                    coroutineScope.launch {
                                        val requestId = selectedRequest.id ?: return@launch
                                        loadingRequestIds.add(requestId) // 🔄 Add to loading set

                                        val success = try {
                                            val response =
                                                RetrofitClient.instance.updateRequestStatus(
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
                                            Toast.makeText(
                                                context,
                                                "Failed to accept request",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                onReject = { selectedRequest ->
                                    coroutineScope.launch {
                                        val requestId = selectedRequest.id ?: return@launch
                                        loadingRequestIds.add(requestId) // 🔄 Add to loading set

                                        val success = try {
                                            val response =
                                                RetrofitClient.instance.updateRequestStatus(
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







