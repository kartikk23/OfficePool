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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.R
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun LiveTrackingMapScreen(
    navController: NavController,
    rideId: String?,
    requestId:String?
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapStyleOptions = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var currentPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }  // Dynamic Polyline
    var staticPolyline by remember { mutableStateOf<List<LatLng>>(emptyList()) }  // Static Polyline (Source to Destination)
    val coroutineScope = rememberCoroutineScope()  // Coroutine scope for launching coroutines
    val apiKey by remember { mutableStateOf<String>(BuildConfig.MAPS_API_KEY) }
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
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(source!!, 15f)

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
                    userLocation = LatLng(location.latitude, location.longitude)

                    // Fetch the updated polyline based on current location
                    userLocation?.let { userLatLng ->
                        destination?.let { destinationLatLng ->
                            coroutineScope.launch {
                                val routeInfo = getRoutePolylineWithInfo(userLatLng, destinationLatLng, apiKey)
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
            }

        }
    } else {
        // 👇 Your actual map and ride tracking UI
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surface)
                .fillMaxSize(),
            ) {

                GoogleMap(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = false,
                        mapType = MapType.NORMAL,
                        isTrafficEnabled = false,
                        mapStyleOptions = mapStyleOptions
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        compassEnabled = false,
                        myLocationButtonEnabled = false,
                        tiltGesturesEnabled = true,
                        scrollGesturesEnabled = true
                    )
                ) {


                    val sourceMarkerState = rememberMarkerState()
                    val destinationMarkerState = rememberMarkerState()
                    val userLocationMarkerState = rememberMarkerState()

                    // Move camera to user location initially
                    LaunchedEffect(userLocation) {
                        userLocation?.let {
                            Log.d("LiveTrackingMap", "Moving camera to user location: $it")
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
                            userLocationMarkerState.position = it
                        }
                    }

                    // Update source marker dynamically
                    LaunchedEffect(source) {
                        source?.let {
                            Log.d("LiveTrackingMap", "Moving camera to source location: $it")
                            sourceMarkerState.position = it
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
                        }
                    }

                    // Update destination marker dynamically
                    LaunchedEffect(destination) {
                        destination?.let {
                            Log.d("LiveTrackingMap", "Moving camera to destination location: $it")
                            destinationMarkerState.position = it
                            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
                        }
                    }

                    // Source Marker
//        if (source != null) {
//            Marker(state = sourceMarkerState, title = sourceTitle,
//                icon = BitmapDescriptorFactory.fromResource(R.drawable.start))
//        }
//
//        // Destination Marker
//        if (destination != null) {
//            Marker(state = destinationMarkerState, title = destinationTitle,
//                    icon = BitmapDescriptorFactory.fromResource(R.drawable.dest))
//        }
                    val startCustomCap = CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.start),
                        25f // Reference width in pixels (adjust as needed)

                    )
                    val endCustomCap = CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.dest),
                        25f // Reference width in pixels (adjust as needed)
                    )

                    // Display the static polyline (light color)
                    if (staticPolyline.isNotEmpty()) {
                        Log.d("LiveTrackingMap", "Drawing static polyline on the map")
                        Polyline(
                            points = staticPolyline,
                            color = Color.Black, // Light-colored polyline
                            width = 12f,
                            startCap = startCustomCap,
                            endCap = endCustomCap,
                            jointType = JointType.DEFAULT
                        )
                    }

                    val customCap = CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.motorcyclet3),
                        35f // Reference width in pixels (adjust as needed)
                    )



                    // Display the dynamic polyline (dark color)
                    if (currentPolyline.isNotEmpty()) {
                        Log.d("LiveTrackingMap", "Drawing dynamic polyline on the map")
                        Polyline(
                            points = currentPolyline,
                            color = Color.Blue, // Dark-colored polyline for user's path
                            width = 12f,
                            startCap = customCap,
                            endCap = RoundCap(),
                            jointType = JointType.DEFAULT
                        )
                    }

                    // Custom User Location Marker
//        if (userLocation != null) {
//            Marker(
//                state = userLocationMarkerState,
//                title = "You",
////                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
//                icon = BitmapDescriptorFactory.fromResource(R.drawable.bike)
//            )
//        }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (travelTime.isNotEmpty() && instructions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Info, contentDescription = "Time", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Estimated Time: $travelTime",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Place, contentDescription = "Distance", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Distance: $travelDistance",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Done, contentDescription = "Next Step", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Next: ${instructions.firstOrNull()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            isLoading1 = true
                            Log.d("LiveTrackingMap", "End Ride button clicked")
                            coroutineScope.launch {
                                try {
                                    // Construct RideInfo with updated status
                                    val updatedRide = rideInfo!!.copy(status = "Completed") // assuming `response` is RideInfo

                                    val response = RetrofitClient.instance.updateRide(updatedRide)
                                    if (response.isSuccessful) {
                                        Log.d("EndRide", "Ride status updated to Completed")


                                        // Update the status of the ride request

                                        val reqId = requestId?: return@launch
                                        coroutineScope.launch {
                                            val success = try {
                                                val response2 = RetrofitClient.instance.updateRequestStatus(
                                                    RideRequestStatusUpdateDTO(
                                                        id = reqId.toLong(),
                                                        requestStatus = "Completed"
                                                    )
                                                )
                                                response2.isSuccessful

                                            } catch (e: Exception) {
                                                isLoading1 = false
                                                e.printStackTrace()
                                                false
                                            }


                                            if (success) {
                                                isLoading1 = false
                                                navController.popBackStack()
//                                            Toast.makeText(context, "Accepted ${selectedRequest.passengerName}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                isLoading1 = false
                                                Toast.makeText(context, "Failed to accept request", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                    } else {
                                        isLoading1 = false
                                        Log.e("EndRide", "Failed to update ride: ${response.code()}")
                                    }
                                } catch (e: Exception) {
                                    isLoading1 = false
                                    Log.e("EndRide", "Exception while updating ride", e)
                                }
                            }

                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading1) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(20.dp))

                        } else {
                            Text("End Ride",
                                Modifier.padding(0.dp,5.dp),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.surface,

                                    )

                            )
                        }

                    }
                }
            }

            // ✅ Top-left Back Button Overlay
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { navController.popBackStack() },
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


    }






}

private fun findNearestPointIndex(userLocation: LatLng, path: List<LatLng>): Int {
    var minDistance = Double.MAX_VALUE
    var nearestIndex = 0
    path.forEachIndexed { index, latLng ->
        val distance = haversine(userLocation.latitude, userLocation.longitude, latLng.latitude, latLng.longitude)
        if (distance < minDistance) {
            minDistance = distance
            nearestIndex = index
        }
    }
    return nearestIndex
}

private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371e3 // Earth radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}


data class RouteInfo(
    val polyline: List<LatLng>,
    val durationText: String,
    val distanceText: String,
    val steps: List<String>
)

suspend fun getRoutePolylineWithInfo(source: LatLng, destination: LatLng, apiKey: String): RouteInfo {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${source.latitude},${source.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&key=$apiKey"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val routes = json.getJSONArray("routes")

            if (routes.length() > 0) {
                val route = routes.getJSONObject(0)
                val polyline = route
                    .getJSONObject("overview_polyline")
                    .getString("points")

                val leg = route.getJSONArray("legs").getJSONObject(0)
                val durationText = leg.getJSONObject("duration").getString("text")
                val distanceText = leg.getJSONObject("distance").getString("text")
                val stepsJson = leg.getJSONArray("steps")
                val steps = mutableListOf<String>()
                for (i in 0 until stepsJson.length()) {
                    val step = stepsJson.getJSONObject(i)
                    val htmlInstruction = step.getString("html_instructions")
                    val cleanText = htmlInstruction.replace(Regex("<[^>]*>"), "")
                    steps.add(cleanText)
                }

                RouteInfo(
                    polyline = decodePolyline(polyline),
                    durationText = durationText,
                    distanceText = distanceText,
                    steps = steps,

                )
            } else {
                RouteInfo(emptyList(), "","", emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RouteInfo(emptyList(), "", "",emptyList())
        }
    }
}




fun decodePolyline(encoded: String): List<LatLng> {
    Log.d("LiveTrackingMap", "Decoding polyline")
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = LatLng(lat / 1E5, lng / 1E5)
        poly.add(p)
    }

    return poly
}
