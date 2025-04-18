package com.agile.officepool.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agile.officepool.model.RideInfo
import com.agile.officepool.model.RideRequest
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableRidesScreen(navController: NavController,
                         sourceName: String,
                         sourceLat: Double,
                         sourceLng: Double,
                         destinationName: String,
                         destinationLat: Double,
                         destinationLng: Double){


    var availableRides by remember { mutableStateOf<List<RideInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            fetchAvailableRides { rides ->
                availableRides = filterNearbyRides(rides, sourceLat, sourceLng)
                isLoading = false
            }
        }
    }

    Scaffold(
    topBar = {
        TopAppBar(title = { Text("Available Rides") })
    }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                availableRides.isEmpty() -> Text("No nearby rides available")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(availableRides) { ride ->
                        RideCard(ride)
                    }
                }
            }
        }
    }
}

@Composable
fun RideCard(ride: RideInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)), // Pure white for clean UI
        shape = RoundedCornerShape(12.dp), // Soft rounded corners
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp) // Gentle shadow
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 20.dp)
                .fillMaxWidth()
        ) {
            // Rider & Ride Start Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rider: ${ride.riderId}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF333333) // Dark text for contrast
                )
                Text(
                    text = ride.rideStartTime,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp),
                    color = Color(0xFF757575) // Muted grey for subtlety
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp) // Soft separator
            Spacer(modifier = Modifier.height(10.dp))

            // Source & Destination Details
            RideDetailItem(label = "Source", value = ride.source)
            RideDetailItem(label = "Destination", value = ride.destination)
            RideDetailItem(label = "Route", value = ride.route)

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp) // Subtle divider
            Spacer(modifier = Modifier.height(10.dp))

            // Seats & Pickup Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Available Seats: ${ride.availableSeats}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF0277BD) // Blue shade for emphasis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFD84315) // Subtle red-orange for location
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pickup Nearby",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF616161) // Soft dark grey
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            val coroutineScope = rememberCoroutineScope();
            val context = LocalContext.current
            val sessionManager = SessionManager(context)

            // Request Ride Button
            Button(
                onClick = {
                    coroutineScope.launch {

                        val passengerId = sessionManager.getUserId() ?: ""
                        val passengerName = sessionManager.getUsername() ?: ""
                        sendRideRequest(
                            passengerId = passengerId,
                            passengerName = passengerName,
                            rideId = ride.rideId!! ,
                            riderId = ride.riderId,
                            context)
                        { success ->
                            if (success) {
                                println("✅ Ride request flow complete")
                            } else {
                                println("❌ Ride request or notification failed")
                            }
                        }
                    }


                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)), // Clean blue
                shape = RoundedCornerShape(8.dp) // Softer but defined button
            ) {
                Text(
                    "Request Ride",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}



fun sendRideRequest(
    passengerId: String,
    passengerName: String,
    rideId:Int,
    riderId: String,
    context: Context,
    onResult: (Boolean) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = RideRequest(
                rideId=rideId.toString(),
                passengerId = passengerId,
                passengerName = passengerName,
                riderId = riderId,
                requestStatus = "REQUESTED")
            Log.d("RIDE_REQUEST", "📤 Sending ride request to backend... $request")
            val response = RetrofitClient.instance.sendRideRequest(request)


            if (response.isSuccessful && response.body()?.success == true) {

                Log.d("RIDE_REQUEST", "✅ Ride request saved: ${response.body()}")

                // Show immediate feedback to user
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ride request saved. Sending notification...", Toast.LENGTH_SHORT).show()
                }

                // 🔔 Trigger FCM Notification to Rider
                val notifyResponse = RetrofitClient.instance.getFCMToken(request)
                val message = notifyResponse.body()
                Log.d("RIDE_REQUEST", "📨 Notification response body: $message")
                Log.d("RIDE_REQUEST", "📨 Notification response status: ${notifyResponse.code()}")


                withContext(Dispatchers.Main) {
                    if (notifyResponse.isSuccessful && message?.get("status") == "success") {
                        Toast.makeText(context, "✅ Ride request notification sent!", Toast.LENGTH_LONG).show()
                        onResult(true)
                    } else {
                        Toast.makeText(context, "⚠\uFE0F FCM failed: ${message?.get("message")}", Toast.LENGTH_LONG).show()
                        onResult(false)
                    }
                }
            } else {
                Log.e("RIDE_REQUEST", "❌ Ride request failed: ${response.errorBody()?.string()}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Ride request failed!", Toast.LENGTH_LONG).show()
                    onResult(false)
                }
            }
        } catch (e: Exception) {
            Log.e("RIDE_REQUEST", "💥 Exception: ${e.localizedMessage}")
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "⚠️ Error sending ride request", Toast.LENGTH_LONG).show()
                onResult(false)
            }
        }
    }
}


@Composable
fun RideDetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF757575) // Soft muted grey
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF333333) // Dark color for good contrast
        )
        Spacer(modifier = Modifier.height(9.dp))
    }
}

fun fetchAvailableRides(onRidesFetched: (List<RideInfo>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.instance.getRideByStatus("Yet To Start")
            val rides = response.body() ?: emptyList()
            withContext(Dispatchers.Main) {
                onRidesFetched(rides)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onRidesFetched(emptyList()) // Return empty list on failure
            }
        }
    }
}

fun filterNearbyRides(rides: List<RideInfo>, sourceLat: Double, sourceLng: Double): List<RideInfo> {
    return rides.filter { ride ->
        val distance = calculateDistance(sourceLat, sourceLng, ride.sourceLat, ride.sourceLng)
        distance <= 2.0 // Show only rides within 1 km
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Radius of Earth in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c // Distance in km
}


