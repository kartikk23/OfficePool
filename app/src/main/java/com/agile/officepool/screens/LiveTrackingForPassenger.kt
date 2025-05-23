package com.agile.officepool.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.agile.officepool.BuildConfig
import com.agile.officepool.R
import com.agile.officepool.ViewModel.SharedRideViewModel
import com.agile.officepool.components.mapComponents.RouteInfoCard
import com.agile.officepool.components.mapComponents.StaticRoutePolyline
import com.agile.officepool.helper.MapHelperFunctions.getRoutePolylineWithInfo
import com.agile.officepool.helper.MapHelperFunctions.observeRiderLocation
import com.agile.officepool.network.RetrofitClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var staticPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var travelTime by remember { mutableStateOf("") }
    var travelDistance by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf<List<String>>(emptyList()) }
    val bitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.motorcyclet3)
            .let { original ->
                Bitmap.createScaledBitmap(original, 150, 150, false)
            }
    }
    val isRideActive by rideViewModel.isRideActive

    // Observe rideActive changes continuously
    LaunchedEffect(isRideActive, rideId) {
        if (isRideActive == true) {
            // Start listening to rider location updates
            observeRiderLocation(rideId) { data ->
                riderLatLng = LatLng(data.latitude, data.longitude)
                coroutineScope.launch {
                    try {
                        val response = RetrofitClient.instance.getRideByRideId(rideId)
                        if (response.isSuccessful) {
                            response.body()?.let { ride ->
                                destination = LatLng(ride.destinationLat, ride.destinationLng)
                                val routeInfo = getRoutePolylineWithInfo(riderLatLng!!, destination!!, apiKey)
                                staticPolyline = routeInfo.polyline
                                travelTime = routeInfo.durationText
                                travelDistance = routeInfo.distanceText
                                instructions = routeInfo.steps
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(riderLatLng!!, 15f))
                            }
                        } else {
                            Log.e("LiveTrackingForPassenger", "Failed to fetch ride details: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("LiveTrackingForPassenger", "Error fetching ride details", e)
                    }
                }
            }
        } else {
            // If ride ended, navigate after a delay
            delay(2000)
            navHostController.navigate("startup") {
                popUpTo("startup") { inclusive = true }
            }
        }
    }

    if (isRideActive == true) {
        // Show map and UI here same as before
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
                    StaticRoutePolyline(polyline = staticPolyline)
                    val riderIcon = remember(bitmap) {
                        BitmapDescriptorFactory.fromBitmap(bitmap)
                    }
                    riderLatLng?.let {
                        Marker(
                            state = MarkerState(position = it),
                            icon = riderIcon,
                            title = "Rider"
                        )
                    }
                }
                Box(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    RouteInfoCard(
                        travelTime = travelTime,
                        travelDistance = travelDistance,
                        instructions = instructions
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(16.dp)
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
        // Show ride ended UI (can omit navigation here since handled in LaunchedEffect)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Your ride has ended.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

