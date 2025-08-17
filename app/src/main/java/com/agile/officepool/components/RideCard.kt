package com.agile.officepool.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agile.OfficePool.utils.SessionManager
import com.agile.officepool.helper.RideRequestHelper.sendRideRequest
import com.agile.officepool.model.RideInfo
import com.agile.officepool.model.RideRequest
import com.agile.officepool.responseDTO.RideInfoResponseDTO
import com.agile.officepool.responseDTO.RideReqResponseDTO
import com.agile.officepool.ui.theme.OfficePoolTheme
import com.agile.officepool.ui.theme.RobotoCondensed
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun RideCard(ride:RideInfoResponseDTO, rideRequest: RideReqResponseDTO?){
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 4.dp),
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
                        text = "From : ${ride.rider.name}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                }


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
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                        text = formattedDateTime,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                }

            }

            val coroutineScope = rememberCoroutineScope();
            val context = LocalContext.current
            val sessionManager = SessionManager(context)
            val passengerId = sessionManager.getUserId() ?: ""
            val passengerName = sessionManager.getUserName() ?: ""
            val initialStatus = rideRequest?.requestStatus ?: ""
            var status by remember { mutableStateOf(initialStatus) }
            val currentStatus = rememberUpdatedState(status)


            // ✅ Log Ride Info and Ride Request details
            Log.d("RIDE_CARD", "🚘 rideId=${ride.id}, riderId=${ride.id}, status=$status")
            Log.d("RIDE_CARD", "📄 rideRequest: $rideRequest")

            // Request Ride Button
            if (currentStatus.value == "") {
                // Show real button when no request has been made
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sendRideRequest(
                                passengerId = passengerId,
                                rideId = ride.id.toInt(),
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
                        fontFamily = RobotoCondensed,
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
                            "Completed" -> "Completed"
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


//@Preview(showBackground = true)
//@Composable
//fun RideCardPreview() {
//    val mockRide = RideInfo(
//        rideId = 344,
//        rider = "Ajay123",
//        source = "Tech Park",
//        destination = "City Center",
//        sourceLat = 18.5204,
//        sourceLng = 18.5204,
//        availableSeats = "2",
//        rideDate = "2025-07-01",
//        rideStartTime = "14:30",
//        destinationLat = 18.5204,
//        destinationLng = 73.8567,
//        route = "Man road",
//        status = "Active"
//    )
//
//    val mockRequest = RideRequest(
//        id = 2,
//        rideId = "R123",
//        riderId = "344",
//        rideFare = 200,
//        passengerId = "P456",
//        passengerName = "Tejas",
//        requestStatus = "REQUESTED"
//    )
//
//    OfficePoolTheme{
//        RideCard(
//            ride = mockRide,
//            rideRequest = mockRequest
//        )
//    }
//}


//@Composable
//fun RideCard2(ride: RideInfo, rideRequest: RideRequest?) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 6.dp, horizontal = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)), // Pure white for clean UI
//        shape = RoundedCornerShape(12.dp), // Soft rounded corners
//        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp) // Gentle shadow
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(vertical = 12.dp, horizontal = 20.dp)
//                .fillMaxWidth()
//        ) {
//            // Rider & Ride Start Time
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = "Rider: ${ride.riderId}",
//                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
//                    color = Color(0xFF333333) // Dark text for contrast
//                )
//                Text(
//                    text = ride.rideStartTime.toString(),
//                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 15.sp),
//                    color = Color(0xFF757575) // Muted grey for subtlety
//                )
//            }
//
//            Spacer(modifier = Modifier.height(10.dp))
//            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp) // Soft separator
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // Source & Destination Details
//            RideDetailItem(label = "Source", value = ride.source)
//            RideDetailItem(label = "Destination", value = ride.destination)
//            RideDetailItem(label = "Route", value = ride.route)
//
//            Spacer(modifier = Modifier.height(10.dp))
//            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp) // Subtle divider
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // Seats & Pickup Section
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = "Available Seats: ${ride.availableSeats}",
//                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
//                    color = Color(0xFF0277BD) // Blue shade for emphasis
//                )
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = Icons.Default.LocationOn,
//                        contentDescription = "Location",
//                        tint = Color(0xFFD84315) // Subtle red-orange for location
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "Pickup Nearby",
//                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
//                        color = Color(0xFF616161) // Soft dark grey
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(15.dp))
//
//            val coroutineScope = rememberCoroutineScope();
//            val context = LocalContext.current
//            val sessionManager = SessionManager(context)
//            val passengerId = sessionManager.getUserId() ?: ""
//            val passengerName = sessionManager.getUsername() ?: ""
//            val initialStatus = rideRequest?.requestStatus ?: ""
//            var status by remember { mutableStateOf(initialStatus) }
//            val currentStatus = rememberUpdatedState(status)
//
//
//            // ✅ Log Ride Info and Ride Request details
//            Log.d("RIDE_CARD", "🚘 rideId=${ride.rideId}, riderId=${ride.riderId}, status=$status")
//            Log.d("RIDE_CARD", "📄 rideRequest: $rideRequest")
//
//            // Request Ride Button
//            if (currentStatus.value == "") {
//                // Show real button when no request has been made
//                Button(
//                    onClick = {
//                        coroutineScope.launch {
//                            sendRideRequest(
//                                passengerId = passengerId,
//                                passengerName = passengerName,
//                                rideId = ride.rideId!!,
//                                riderId = ride.riderId,
//                                context
//                            ) { success ->
//                                if (success) {
//                                    println("✅ Ride request flow complete")
//                                    status = "REQUESTED"
//                                } else {
//                                    println("❌ Ride request or notification failed")
//                                }
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .wrapContentHeight(),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text(
//                        "Request Ride",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp,
//                        color = Color.White
//                    )
//                }
//            } else {
//                // Show a text-style fake button (disabled style)
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                        .background(
//                            color = when (status) {
//                                "REQUESTED" -> Color.LightGray
//                                "ACCEPTED" -> Color(0xFF43A047) // Green
//                                "REJECTED" -> Color(0xFFE53935) // Red
//                                else -> Color.Gray
//                            },
//                            shape = RoundedCornerShape(8.dp)
//                        )
//                        .padding(vertical = 12.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = when (status) {
//                            "REQUESTED" -> "Requested"
//                            "ACCEPTED" -> "Accepted"
//                            "REJECTED" -> "Rejected"
//                            "COMPLETED" -> "Completed"
//                            else -> "Ride Status"
//                        },
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 16.sp,
//                        color = Color.White
//                    )
//                }
//            }
//
//        }
//    }
//}

