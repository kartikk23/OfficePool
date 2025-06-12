import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.R
import com.agile.officepool.ViewModel.SharedRideViewModel
import com.agile.officepool.components.mapComponents.DynamicRoutePolyline
import com.agile.officepool.components.mapComponents.RouteInfoCard
import com.agile.officepool.components.mapComponents.StaticRoutePolyline
import com.agile.officepool.helper.MapHelperFunctions.deleteLocationFromFirebase
import com.agile.officepool.helper.MapHelperFunctions.getRoutePolylineWithInfo
import com.agile.officepool.helper.MapHelperFunctions.pushLocationToFirebase
import com.agile.officepool.model.CompleteRideDTO
import com.agile.officepool.model.RideInfo
import com.agile.officepool.model.RideRequestStatusUpdateDTO
import com.agile.officepool.network.RetrofitClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

@Composable
fun LiveTrackingMapScreen(
    navController: NavController,
    rideId: String?,
    requestId:String?,
    sharedRideViewModel: SharedRideViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapStyleOptions = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
    var riderLocation by remember { mutableStateOf<LatLng?>(null) }
    var currentPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }  // Dynamic Polyline
    var staticPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }  // Static Polyline (Source to Destination)
    val coroutineScope = rememberCoroutineScope()  // Coroutine scope for launching coroutines
    val apiKey by remember { mutableStateOf(BuildConfig.MAPS_API_KEY) }
    var travelTime by remember { mutableStateOf("") }
    var travelDistance by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf<List<String>>(emptyList()) }
    var source by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var sourceTitle by remember { mutableStateOf<String?>(null) }
    var destinationTitle by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoading1 by remember { mutableStateOf(false) }
    var rideInfo by  remember {mutableStateOf<RideInfo?>(null)}
    val cameraPositionState = rememberCameraPositionState()


    LaunchedEffect(rideId) {
        if (rideId != null) {
            Log.d("LiveTrackingMap", "Fetching ride details for rideId: $rideId")
            coroutineScope.launch {
                try {
                    val response = RetrofitClient.instance.getRideByRideId(rideId)
                    if (response.isSuccessful) {
                        Log.d("LiveTrackingMap", "Received ride details successfully")
                        response.body()?.let { ride ->

                            rideInfo = ride
                            source = LatLng(ride.sourceLat, ride.sourceLng)
                            sourceTitle = ride.source
                            destination = LatLng(ride.destinationLat, ride.destinationLng)
                            destinationTitle = ride.destination

                            // Fetch polyline in a coroutine (static route)
                            source?.let { src ->
                                destination?.let { dest ->
                                    Log.d("LiveTrackingMap", "Fetching static polyline from $src to $dest")
                                    val routeInfo = getRoutePolylineWithInfo(source!!, destination!!, apiKey)
                                    staticPolyline = routeInfo.polyline
                                }
                            }
                            // Update camera to source
                            riderLocation?.let {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 18f)
                            } ?: Log.d("LiveTrackingMap", "riderLocation is still null")

                            // ✅ Mark loading complete
                            isLoading = false
                        }
                    } else {
                        Log.e("LiveTrackingMap", "Failed to fetch ride details: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("LiveTrackingMap", "Error fetching ride details", e)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LiveTrackingMap", "Location permission granted, requesting location updates")

            val locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    riderLocation = LatLng(location.latitude, location.longitude)

                    pushLocationToFirebase(rideId = rideId ?: "", location = riderLocation!!)

                    // Fetch the updated polyline based on current location
                    riderLocation?.let { userLatLng ->
                        destination?.let { destinationLatLng ->
                            coroutineScope.launch {
                                val routeInfo = getRoutePolylineWithInfo(
                                    userLatLng,
                                    destinationLatLng,
                                    apiKey
                                )
                                currentPolyline = routeInfo.polyline
                                travelTime = routeInfo.durationText
                                travelDistance = routeInfo.distanceText
                                instructions = routeInfo.steps
                            }
                        } ?: Log.e("LiveTrackingMap", "Destination is null")
                    } ?: Log.e("LiveTrackingMap", "User location is null")
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            // ✅ Cleanup when Composable leaves the Composition
            onDispose {
                Log.d("LiveTrackingMap", "Removing location updates")
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } else {
            Log.e("LiveTrackingMap", "Location permission denied")
            onDispose { }
        }
    }

    if (isLoading) {
        // Show loader while fetching data
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Preparing Map...", color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp))
                CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
            }

        }
    } else {
        rideInfo?.let {

            Box(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
                    GoogleMap(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(
                            isMyLocationEnabled = false,
                            mapType = MapType.NORMAL,
                            isTrafficEnabled = false,
                            mapStyleOptions = mapStyleOptions,
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            compassEnabled = false,
                            myLocationButtonEnabled = false
                        )
                    ) {
                        StaticRoutePolyline(staticPolyline)
                        DynamicRoutePolyline(currentPolyline)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surface)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Info card
                        RouteInfoCard(
                            travelTime = travelTime,
                            travelDistance = travelDistance,
                            instructions = instructions
                        )

                        Button(
                            onClick = {
                                isLoading1 = true
                                Log.d("LiveTrackingMap", "End Ride button clicked")
                                coroutineScope.launch {
                                    try {
                                        val rideIdVal = rideId ?: return@launch
                                        val reqIdVal = requestId ?: return@launch

                                        val dto = CompleteRideDTO(
                                            rideId = rideIdVal.toInt(),
                                            rideRequestId = reqIdVal.toLong()
                                        )

                                        val response = RetrofitClient.instance.completeRideAndRequestStatus(dto)

                                        isLoading1 = false

                                        if (response.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "End Ride Successful",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            sharedRideViewModel.setRideActive(false)
                                            deleteLocationFromFirebase(rideIdVal) // 🧹 Delete Firebase node

                                            // Update local ViewModel with updated status
                                            val updatedRide = rideInfo!!.copy(status = "Completed")
                                            sharedRideViewModel.updateRideInfo(updatedRide)

                                            Log.d("updatedRide", updatedRide.toString())
                                            Log.d("sharedrideInfo", sharedRideViewModel.rideInfo.value.toString())

                                            navController.navigate("riderPayment") {
                                                popUpTo("rideRequests") { inclusive = true }
                                            }

                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to complete ride",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            Log.e("EndRide", "Failed with code: ${response.code()}")
                                        }

                                    } catch (e: Exception) {
                                        isLoading1 = false
                                        Log.e("EndRide", "Exception occurred", e)
                                    }
                                }
                            },
                            enabled = !isLoading1,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading1) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    "End Ride",
                                    Modifier.padding(0.dp, 5.dp),
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }

                    }

                }
//                // ✅ Top-left Back Button Overlay
//                Box(
//                    modifier = Modifier
//                        .padding(16.dp)
//                        .align(Alignment.TopStart)
//                        .size(40.dp)
//                        .clip(CircleShape)
//                        .background(Color.Black.copy(alpha = 0.6f))
//                        .clickable { navController.popBackStack() },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                        contentDescription = "Back",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }

            }
        }
    }

}
