package com.agile.officepool.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.agile.officepool.BuildConfig
import com.agile.officepool.R
import com.agile.officepool.viewModel.SharedRideViewModel
import com.agile.officepool.components.mapComponents.DynamicRoutePolyline
import com.agile.officepool.components.mapComponents.RouteInfoCard
import com.agile.officepool.helper.MapHelperFunctions.getRoutePolylineWithInfo
import com.agile.officepool.helper.MapHelperFunctions.observeRiderLocation
import com.agile.officepool.network.RetrofitClient
import com.agile.OfficePool.utils.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.tooling.preview.Preview
import com.agile.officepool.components.RideCompletedCard
import com.agile.officepool.responseDTO.RideInfoResponseDTO


@Composable
fun LiveTrackingForPassenger(
    navHostController: NavHostController,
    rideId: String,
    rideViewModel: SharedRideViewModel = viewModel()
) {
    val context = LocalContext.current
    val mapStyle = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    val cameraPositionState = rememberCameraPositionState()
    val apiKey by remember { mutableStateOf(BuildConfig.MAPS_API_KEY) }

    var riderLatLng by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var currentPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var travelTime by remember { mutableStateOf("") }
    var travelDistance by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf<List<String>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val sessionManager = SessionManager(context)
    val passengerId = sessionManager.getUserId() ?: ""
    val isRideActive by rideViewModel.isRideActive

    var rideInfo = remember { mutableStateOf<RideInfoResponseDTO?>(null) }
    var rideFare by remember { mutableStateOf<Int?>(null) }
    var riderName by remember { mutableStateOf<String?>(null) }
    var riderUpiId by remember { mutableStateOf<String?>(null) }
    var isFareLoading by remember { mutableStateOf(false) }

    val bitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.motorcyclet3)
            .let { Bitmap.createScaledBitmap(it, 150, 150, false) }
    }

    // Fetch route data when ride is active
    LaunchedEffect(isRideActive, rideId) {
        if (isRideActive == true) {
            observeRiderLocation(rideId) { data ->
                riderLatLng = LatLng(data.latitude, data.longitude)
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.instance.getRideByRideId(rideId)
                        if (response.isSuccessful) {
                            response.body()?.let { ride ->
                                destination = LatLng(ride.destinationLat, ride.destinationLng)
                                val routeInfo = getRoutePolylineWithInfo(riderLatLng!!, destination!!, apiKey)
                                currentPolyline = routeInfo.polyline
                                travelTime = routeInfo.durationText
                                travelDistance = routeInfo.distanceText
                                instructions = routeInfo.steps
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(riderLatLng!!, 18f))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LiveTracking", "Error fetching ride: ${e.localizedMessage}")
                    }
                }
            }

            rideViewModel.observePassengerRideStatus(
                rideId = rideId,
                onStarted = { rideViewModel.setRideActive(true) },
                onNotActive = { rideViewModel.setRideActive(false) }
            )
        }
    }

    // Fetch fare and UPI ID once ride ends
    // Inside LaunchedEffect (fare and UPI fetching logic)
    LaunchedEffect(key1 = isRideActive, key2 = rideId) {
        if (!isRideActive!! && rideFare == null) {
            isFareLoading = true
            coroutineScope.launch {
                try {
                    // 🔹 Fetch Ride Fare
                    val reqWithRide = RetrofitClient.instance.getRideRequest(rideId, passengerId)
                    if (reqWithRide.isSuccessful) {
                        reqWithRide.body()?.let {
                            rideFare = it.rideFare
                            Log.d("FareAPI", "✅ Fare fetched: ₹${it.rideFare}")
                        }
                    } else {
                        Log.e("FareAPI", "❌ Failed to fetch fare: ${reqWithRide.code()} ${reqWithRide.message()}")
                    }

                        val ride = reqWithRide.body()!!.ride
                        rideInfo.value = ride
                        // 🔹 Fetch Rider Info using riderId from ride
                        val rider = ride.rider
                        riderUpiId = rider.upiId
                        riderName = rider.name ?: "Unknown Rider"
                        Log.d("UPI_FETCH", "✅ Rider info from ride: $riderName, UPI: $riderUpiId")
                        Log.d("UPI_FETCH", "✅ Rider ID from ride: ${rider.id}")

                } catch (e: Exception) {
                    Log.e("FareAPI", "❌ Exception occurred: ${e.localizedMessage}", e)
                } finally {
                    isFareLoading = false
                }
            }
        }
    }




    DisposableEffect(rideId) {
        onDispose { rideViewModel.removeRideStatusListener(rideId) }
    }

    if (isRideActive == true) {
        // Show active tracking
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapType = MapType.NORMAL,
                        mapStyleOptions = mapStyle
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    DynamicRoutePolyline(polyline = currentPolyline)
                }

                Box(modifier = Modifier.padding(16.dp)) {
                    RouteInfoCard(
                        travelTime = travelTime,
                        travelDistance = travelDistance,
                        instructions = instructions
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .align(Alignment.TopStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { navHostController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    } else {
        // Show fare & UPI payment
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            RideEndedFareSection(
                isFareLoading = isFareLoading,
                rideFare = rideFare,
                riderUpiId = riderUpiId,
                riderName = riderName,
                from = rideInfo.value?.source ?: "Unknown",
                to = rideInfo.value?.destination ?: "Unknown",
                onPayClicked = {
                    Log.d("PaymentIntent", "Launching UPI for ₹$rideFare to $riderUpiId")

                    val uri = Uri.parse(
                        "upi://pay?pa=$riderUpiId&pn=OfficePool&tn=Ride Fare&am=$rideFare&cu=INR"
                    )
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    val chooser = Intent.createChooser(intent, "Pay with UPI")
                    context.startActivity(chooser)
                }
            )
        }
    }
}

@Composable
fun RideEndedFareSection(
    isFareLoading: Boolean,
    rideFare: Int?,
    riderUpiId: String?,
    riderName: String?, // Add defaults or pass as needed
    from: String ?,
    to: String?,
    onPayClicked: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isFareLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text("Fetching fare details...")
        } else {
            if (rideFare != null && !riderUpiId.isNullOrEmpty()) {
                RideCompletedCard(
                    riderName = riderName!!,
                    from = from!!,
                    to = to!!,
                    fareAmount = "₹$rideFare",
                    upiId = riderUpiId,
                    onPayClick = onPayClicked
                )
            } else {
                Text("Fare or UPI ID not available.")
            }
        }
    }
}


@Preview(showBackground = true, apiLevel = 33)
@Composable
fun RideEndedFareSectionPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        RideEndedFareSection(
            isFareLoading = false,
            rideFare = 150,
            riderUpiId = "testupi@bank",
            riderName = "Tejas Katke", // Add defaults or pass as needed
            from = "Cognizant CDC",
            to = "Godrej Green Cove",
        )
    }
}