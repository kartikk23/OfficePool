package com.agile.officepool.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
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
    var rideRequests by remember { mutableStateOf<List<RideRequest>>(emptyList()) }
    val passengerId = SessionManager(LocalContext.current).getUserId() ?: ""

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            fetchAvailableRides { rides ->
                availableRides = filterNearbyRides(rides, sourceLat, sourceLng, destinationLat, destinationLng)
                availableRides = availableRides.sortedByDescending { it.dateTime }
                isLoading = false
            }
            fetchRideRequestsForPassenger(passengerId) { requests ->
                rideRequests = requests
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    items(availableRides) { ride ->
                        val matchingRequest = rideRequests.find { it.rideId == ride.rideId.toString() }
                        RideCard2(ride = ride, rideRequest = matchingRequest)
                    }
                }
            }
        }
    }
}


@Composable
fun RideCard2(ride:RideInfo, rideRequest: RideRequest?){
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp) // Gentle shadow
    ){
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.Top
                ){
                    Text(
                        modifier = Modifier.weight(6f),
                        text = "${ride.source} to ${ride.destination} Trip",
                        color = MaterialTheme.colorScheme.surface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                        Box(
                            modifier = Modifier.weight(4f)
                                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
                                text = "${ride.availableSeats} seat available",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(4.dp)
                    )
                    Text(
                        text = "From : ${ride.riderId}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                }
//                Box(
//                    modifier = Modifier.wrapContentWidth()
//                        .background(color = Color.LightGray, shape = RoundedCornerShape(8.dp))
//                        .padding(horizontal = 10.dp)
//                ){
//                    Text(
//                        text = "${ride.availableSeats} seat",
//                        color = Color.Gray,
//                        fontSize = 14.sp
//                    )
//                }

                val formattedDateTime = try {
                    val date =  LocalDate.parse(ride.rideDate).format(DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH))
                    val time = LocalTime.parse(ride.rideStartTime).format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
                    "$date at $time"
                } catch (e: Exception) {
                    "${ride.rideDate} at ${ride.rideStartTime}"
                }


                Box(
                    modifier = Modifier.wrapContentWidth().wrapContentHeight()
                        .background(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        text = formattedDateTime,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                }

            }
//            Row(
//                verticalAlignment = Alignment.Top,
//                horizontalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                Column(
//                    modifier = Modifier.weight(1.5f),
//                    verticalArrangement = Arrangement.Top
//                ) {
//                    // Profile Image
//                    Box(
//                        modifier = Modifier
//                            .clip(RoundedCornerShape(50))
//                            .background(color = MaterialTheme.colorScheme.surface)
//
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.onSurface,
//                            modifier = Modifier
//                                .size(45.dp)
//                                .align(Alignment.Center).padding(3.dp)
//                        )
//                    }
//                }
//
//
//            }
            val coroutineScope = rememberCoroutineScope();
            val context = LocalContext.current
            val sessionManager = SessionManager(context)
            val passengerId = sessionManager.getUserId() ?: ""
            val passengerName = sessionManager.getUsername() ?: ""
            val initialStatus = rideRequest?.requestStatus ?: ""
            var status by remember { mutableStateOf(initialStatus) }
            val currentStatus = rememberUpdatedState(status)


            // ✅ Log Ride Info and Ride Request details
            Log.d("RIDE_CARD", "🚘 rideId=${ride.rideId}, riderId=${ride.riderId}, status=$status")
            Log.d("RIDE_CARD", "📄 rideRequest: $rideRequest")

            // Request Ride Button
            if (currentStatus.value == "") {
                // Show real button when no request has been made
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sendRideRequest(
                                passengerId = passengerId,
                                passengerName = passengerName,
                                rideId = ride.rideId!!,
                                riderId = ride.riderId,
                                context
                            ) { success ->
                                if (success) {
                                    println("✅ Ride request flow complete")
                                    status = "REQUESTED"
                                } else {
                                    println("❌ Ride request or notification failed")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Request Ride",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            } else {
                // Show a text-style fake button (disabled style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = when (status) {
                                "REQUESTED" -> Color.LightGray
                                "ACCEPTED" -> Color(0xFF43A047) // Green
                                "REJECTED" -> Color(0xFFE53935) // Red
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (status) {
                            "REQUESTED" -> "Requested"
                            "ACCEPTED" -> "Accepted"
                            "REJECTED" -> "Rejected"
                            "COMPLETED" -> "Completed"
                            else -> "Ride Status"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }


    }
}

@Composable
fun RideCard(ride: RideInfo, rideRequest: RideRequest?) {
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
            val passengerId = sessionManager.getUserId() ?: ""
            val passengerName = sessionManager.getUsername() ?: ""
            val initialStatus = rideRequest?.requestStatus ?: ""
            var status by remember { mutableStateOf(initialStatus) }
            val currentStatus = rememberUpdatedState(status)


            // ✅ Log Ride Info and Ride Request details
            Log.d("RIDE_CARD", "🚘 rideId=${ride.rideId}, riderId=${ride.riderId}, status=$status")
            Log.d("RIDE_CARD", "📄 rideRequest: $rideRequest")

            // Request Ride Button
            if (currentStatus.value == "") {
                // Show real button when no request has been made
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sendRideRequest(
                                passengerId = passengerId,
                                passengerName = passengerName,
                                rideId = ride.rideId!!,
                                riderId = ride.riderId,
                                context
                            ) { success ->
                                if (success) {
                                    println("✅ Ride request flow complete")
                                    status = "REQUESTED"
                                } else {
                                    println("❌ Ride request or notification failed")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Request Ride",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            } else {
                // Show a text-style fake button (disabled style)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = when (status) {
                                "REQUESTED" -> Color.LightGray
                                "ACCEPTED" -> Color(0xFF43A047) // Green
                                "REJECTED" -> Color(0xFFE53935) // Red
                                else -> Color.Gray
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (status) {
                            "REQUESTED" -> "Requested"
                            "ACCEPTED" -> "Accepted"
                            "REJECTED" -> "Rejected"
                            "COMPLETED" -> "Completed"
                            else -> "Ride Status"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
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

            val response = RetrofitClient.instance.addRideReq(request);


            if (response.isSuccessful && response.body()?.success == true) {

                Log.d("RIDE_REQUEST", "✅ Ride request saved: ${response.body()}")

                // Show immediate feedback to user
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ride request saved. Sending notification...", Toast.LENGTH_SHORT).show()
                }

                // 🔔 Trigger FCM Notification to Rider
                val notifyResponse = RetrofitClient.instance.sendNotificationToRider(request)
                val res = notifyResponse.body()
                Log.d("RIDE_REQUEST", "📨 Notification response body: $res")
                Log.d("RIDE_REQUEST", "📨 Notification response status: ${notifyResponse.code()}")




                withContext(Dispatchers.Main) {
                    if (res != null) {
                        if (notifyResponse.isSuccessful && res.success){
                            Toast.makeText(context, "✅ Ride request notification sent!", Toast.LENGTH_LONG).show()
                            onResult(true)
                        } else {
                            Toast.makeText(context, "⚠\uFE0F FCM failed: ${res.message}", Toast.LENGTH_LONG).show()
                            onResult(false)
                        }
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
            val response = RetrofitClient.instance.getAllRides()
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

fun filterNearbyRides(rides: List<RideInfo>, sourceLat: Double, sourceLng: Double, destinationLat: Double, destinationLng: Double): List<RideInfo> {
    return rides.filter { ride ->
        val srcDistance = calculateDistance(sourceLat, sourceLng, ride.sourceLat, ride.sourceLng)
        val destDistance = calculateDistance(destinationLat, destinationLng, ride.destinationLat, ride.destinationLng)
        srcDistance <= 2.0 && destDistance <= 2.0 // Show only rides within 2 km
    }
}

fun fetchRideRequestsForPassenger(
    passengerId: String,
    onRequestsFetched: (List<RideRequest>) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.instance.getAllReqByPassengerId(passengerId.toLong())
            val requests = response.body() ?: emptyList()
            withContext(Dispatchers.Main) {
                onRequestsFetched(requests)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onRequestsFetched(emptyList())
            }
        }
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


