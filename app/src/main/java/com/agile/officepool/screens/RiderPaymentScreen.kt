package com.agile.officepool.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.agile.officepool.viewModel.SharedRideViewModel
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.responseDTO.RideInfoResponseDTO
import com.agile.officepool.ui.theme.RobotoCondensed
import kotlinx.coroutines.launch

@Composable
fun RiderPaymentScreen(
    navController: NavHostController,
    rideViewModel: SharedRideViewModel,
    onPaymentConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val rideInfo = rideViewModel.rideInfo.value
    var fare by remember { mutableStateOf<Int?>(null) }
    var isFareLoading by remember { mutableStateOf(true) }

    Log.d("riderModel", rideInfo.toString())

    // Fetch fare from API
    LaunchedEffect(rideInfo?.id) {
        rideInfo?.id?.let { rideId ->
            coroutineScope.launch {
                try {
                    val response = RetrofitClient.instance.getRideRequestByRideId(rideId.toString())
                    if (response.isSuccessful) {
                        response.body()?.let { rideRequest ->
                            fare = rideRequest.rideFare
                        }
                    } else {
                        Log.e("RiderPaymentScreen", "Ride request not found")
                    }
                } catch (e: Exception) {
                    Log.e("RiderPaymentScreen", "Error fetching fare", e)
                } finally {
                    isFareLoading = false
                }
            }
        }
    }

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
        Text(
            "Ride Summary",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = RobotoCondensed
        )

        RideInfoCard(rideInfo)

        if (isFareLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = "Total Fare: ₹${fare ?: "--"}",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = RobotoCondensed
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                Toast.makeText(context, "Payment of ₹$fare Accepted", Toast.LENGTH_SHORT).show()
                onPaymentConfirmed()
            },
            enabled = fare != null && !isFareLoading
        ) {
            Text("Accept ₹${fare ?: "--"}", fontFamily = RobotoCondensed)
        }
    }
}

@Composable
fun RideInfoCard(rideInfo: RideInfoResponseDTO) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "From: ${rideInfo.source}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.surface
            )
            Text(
                "To: ${rideInfo.destination}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}