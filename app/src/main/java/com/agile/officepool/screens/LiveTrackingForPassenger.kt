package com.agile.officepool.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.agile.officepool.BuildConfig
import com.agile.officepool.R
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
import kotlinx.coroutines.launch

@Composable
fun LiveTrackingForPassenger(
    navHostController: NavHostController,
    rideId: String,

) {
    val context = LocalContext.current
    val mapStyle = MapStyleOptions.loadRawResourceStyle(context,R.raw.map_style)
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
                Bitmap.createScaledBitmap(original, 150, 150, false) // width and height in px
            }
    }



    // Set up Firebase listener
    LaunchedEffect(rideId) {
        observeRiderLocation(rideId) { data ->
            riderLatLng = LatLng(data.latitude, data.longitude)
            coroutineScope.launch {
                try {
                    val response = RetrofitClient.instance.getRideByRideId(rideId)
                    if (response.isSuccessful) {
                        Log.d("LiveTrackingForPassenger", "Received ride details successfully")
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
    }

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
                    title = "Rider",
                )
            }
        }

        RouteInfoCard(
            time = travelTime,
            distance = travelDistance,
            steps = instructions
        )
    }
}
