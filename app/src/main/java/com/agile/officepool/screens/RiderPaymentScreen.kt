package com.agile.officepool.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.ViewModel.SharedRideViewModel
import com.agile.officepool.model.RideInfo
import com.agile.officepool.ui.theme.RobotoCondensed


@Composable
fun RiderPaymentScreen(
    navController: NavHostController,
    rideViewModel: SharedRideViewModel,
    onPaymentConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val rideInfo = rideViewModel.rideInfo.value
    Log.d("riderModel", rideInfo.toString())

    if (rideInfo == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(20.dp)
            .padding(WindowInsets.statusBars.asPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ride Summary", style = MaterialTheme.typography.headlineLarge,color = MaterialTheme.colorScheme.onSurface,
            fontFamily = RobotoCondensed
        )

        RideInfoCard(rideInfo)

        Text(
            text = "Total Fare: ₹0",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = RobotoCondensed
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                Toast.makeText(context, "Payment Successful", Toast.LENGTH_SHORT).show()
                onPaymentConfirmed() // <-- Ensures screen is popped after confirmation
            }
        ) {
            Text("Accept ₹0",
                fontFamily = RobotoCondensed)
        }
    }
}
@Composable
fun RideInfoCard(rideInfo: RideInfo) {
    Card (
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("From: ${rideInfo.source}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.surface)
            Text("To: ${rideInfo.destination}", style = MaterialTheme.typography.headlineMedium,color = MaterialTheme.colorScheme.surface)
//            Text("Time: ${rideInfo.rideTime}", style = MaterialTheme.typography.bodyMedium)
//            Text("Seats Booked: ${rideInfo.seatsBooked}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


//@Preview(showBackground = true)
//@Composable
//fun RiderPaymentScreenPreview() {
//    val mockViewModel = SharedRideViewModel().apply {
//        updateRideInfo(
//            RideInfo(
//                rideId = 12,
//                riderId = "23",
//                source = "MG Road",
//                destination = "Koramangala",
//                sourceLat = 12.9716,
//                sourceLng = 77.5946,
//                destinationLat = 12.9352,
//                destinationLng = 77.6146,
//                route = "3e",
//                status = "pending",
//                availableSeats = "4",
//                rideStartTime = "today",
//                rideDate = "today",
//                dateTime = "34"
////                rideFare = rideFare
//            )
//        )
//    }
//
//    RiderPaymentScreen(
//        navController = rememberNavController(),
//        rideViewModel = mockViewModel,
//        onPaymentConfirmed = {}
//    )
//}