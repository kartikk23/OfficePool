package com.agile.officepool.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.agile.officepool.model.RideRequest
import com.agile.officepool.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.model.RideRequestStatusUpdateDTO


@Composable
fun RideRequestCard(
    request: RideRequest,
    isLoading1: Boolean = false,
    onAccept: (RideRequest) -> Unit = {},
    onReject: (RideRequest) -> Unit = {},
    navController: NavController,
    coroutineScope: CoroutineScope
) {
    var isLoading by remember { mutableStateOf(false) }

    var rideStatus by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(request.rideId) {
        try {
            isLoading = true
            val response = RetrofitClient.instance.getRideByRideId(request.rideId)
            if (response.isSuccessful) {
                rideStatus = response.body()?.status
            }
            isLoading = false
        } catch (e: Exception) {
            Log.e("RideRequestCard", "Failed to fetch ride status", e)
        }finally {
            isLoading = false
        }
    }


    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        modifier = Modifier
            .fillMaxWidth()

    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // Row for Passenger Name, Ride ID, and Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.passengerName.trim().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ride ID: ${request.rideId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(
                    text = request.requestStatus.lowercase().replaceFirstChar { it.uppercase() },
                    color = when (request.requestStatus.uppercase()) {
                        "ACCEPTED" -> MaterialTheme.colorScheme.primary
                        "REJECTED" -> MaterialTheme.colorScheme.error
                        "COMPLETED" -> Color.Gray
                        "ACTIVE"-> Color.Green
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }


            // Dynamic content based on request status
            when (request.requestStatus.uppercase()) {
                "REQUESTED" -> {
                    if (isLoading1) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onAccept(request) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accept",style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                            }

                            OutlinedButton(
                                onClick = { onReject(request) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reject",style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }

                "ACTIVE" -> {
                    OutlinedButton(
                        onClick = {

                            navController.navigate("liveTrackingMap/${request.rideId}/${request.id}")

                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Ride in Progress",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                ),
                            )
                        }
                    }
                }

                "ACCEPTED" -> {
                    OutlinedButton(
                        onClick = {
                            isLoading = true

                            coroutineScope.launch {
                                try {
//                                    val response = RetrofitClient.instance.startRideAndNotifyPassenger(request)

//                                    if (response.isSuccessful) {
                                        // 2. Update the ride status (assuming you have an API like updateRideStatus(rideId, "ACTIVE"))
                                        val updateStatusResponse = RetrofitClient.instance.updateRideRequestStatus(
                                            RideRequestStatusUpdateDTO(request.id!!, "Active")
                                        )

                                        if (updateStatusResponse.isSuccessful) {
                                            // 3. Navigate only if both APIs succeeded
                                            navController.navigate("liveTrackingMap/${request.rideId}/${request.id}")
                                        } else {
                                            Log.e("RideRequestCard", "Failed to update ride status: ${updateStatusResponse.errorBody()?.string()}")
                                        }
//                                    } else {
//                                        Log.e("RideRequestCard", "Failed to start ride: ${response.errorBody()?.string()}")
//                                    }
                                } catch (e: Exception) {
                                    Log.e("RideRequestCard", "Exception in starting ride", e)
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Start Ride",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun StatusChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}





@Preview(showBackground = true)
@Composable
fun PreviewRideRequestCardRequested() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    RideRequestCard(
        request = RideRequest(
            id = 1,
            rideId = "101",
            passengerName = "John Doe",
            requestStatus = "REQUESTED",
            passengerId = "200",
            riderId = "300",
            requestTime = "2023-09-10T15:30:00"
        ),
        isLoading1 = false,
        navController = navController,
        coroutineScope = coroutineScope
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRideRequestCardAccepted() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    RideRequestCard(
        request = RideRequest(
            id = 2,
            rideId = "102",
            passengerName = "Jane Smith",
            requestStatus = "ACCEPTED",
            passengerId = "200",
            riderId = "300",
            requestTime = "2023-09-10T15:30:00"
        ),
        navController = navController,
        coroutineScope = coroutineScope
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRideRequestCardRejected() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    RideRequestCard(
        request = RideRequest(
            id = 3,
            rideId = "103",
            passengerName = "Alice Brown",
            requestStatus = "REJECTED",
            passengerId = "200",
            riderId = "300",
            requestTime = "2023-09-10T15:30:00"
        ),
        navController = navController,
        coroutineScope = coroutineScope
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewRideRequestCardCompleted() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    RideRequestCard(
        request = RideRequest(
            id = 4,
            rideId = "104",
            passengerName = "Bob Johnson",
            requestStatus = "COMPLETED",
            passengerId = "200",
            riderId = "300",
            requestTime = "2023-09-10T15:30:00"
        ),
        navController = navController,
        coroutineScope = coroutineScope
    )
}

