package com.agile.officepool.screens

import GooglePlacesDropdown
import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.agile.officepool.components.TimePicker
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.MainActivity
import com.agile.officepool.R
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.RoundCap
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var name by remember { mutableStateOf(TextFieldValue("")) }

    var source by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var selectedSource by remember { mutableStateOf("") }
    var selectedDestination by remember { mutableStateOf("") }
    var sourceLat by remember { mutableStateOf<Double?>(null) }
    var sourceLng by remember { mutableStateOf<Double?>(null) }

    var destinationLat by remember { mutableStateOf<Double?>(null) }
    var destinationLng by remember { mutableStateOf<Double?>(null) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    var location by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    var showModal by remember { mutableStateOf(false)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showSearchbar by remember { mutableStateOf(true)}
    var isLoading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }

    val sessionManager = remember { SessionManager(context) }

    val userEmail = sessionManager.getUserEmail()
    val isUserLoggedIn = sessionManager.isUserLoggedIn()
    var isRider by remember { mutableStateOf(false) }
    var route by remember { mutableStateOf("") }
    var rideStartTime by remember { mutableStateOf("") }
    val statusOptions = listOf("Yet To Start", "Active", "Completed","Cancelled", ) // ✅ Dropdown options
    var status by remember { mutableStateOf(statusOptions[0]) } // Default selection
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    var availableSeats by remember { mutableStateOf("") }
    val userId = sessionManager.getUserId() ?: ""



    // Initialize the Places Client
    fun initializePlacesClient(context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }
    val placesClient = remember { initializePlacesClient(context) }

    var placePredictions by remember { mutableStateOf(emptyList<AutocompletePrediction>()) }

//    TransparentStatusBar()

    fun fetchPlaces(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                placePredictions = response.autocompletePredictions
            }
            .addOnFailureListener { exception ->
                placePredictions = emptyList()
            }
    }

    fun submitRide() {
        coroutineScope.launch {
            if (selectedSource.isNullOrBlank() || selectedDestination.isNullOrBlank() ||
                rideStartTime.isBlank() || route.isBlank() || status.isBlank() || availableSeats.isBlank()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show()
                return@launch
            }

            val rideInfo = RideInfo(
                riderId = userId,
                source = selectedSource!!,
                destination = selectedDestination!!,
                sourceLat = sourceLat!!,
                sourceLng = sourceLng!!,
                destinationLat = destinationLat!!,
                destinationLng = destinationLng!!,
                route = route,
                status = status,
                availableSeats = availableSeats,
                rideStartTime = rideStartTime
            )

            try {
                val response = RetrofitClient.instance.addRide(rideInfo)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Ride added successfully!", Toast.LENGTH_LONG).show()
                    navController.navigate("home") // Navigate after success
                } else {
                    Toast.makeText(context, "Failed to add ride", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        }







    // Request location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            hasLocationPermission = true
            val (loc, address) = fetchUserLocation(context, fusedLocationClient)
            if (loc != null) {
                userLocation = loc
                location = address ?: "Unknown Location"
            }
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }

        if (!userEmail.isNullOrEmpty()) {
            Toast.makeText(context, "Logged in as: $userEmail", Toast.LENGTH_LONG).show()
        }




//        check for incomplete profile
        val phone = sessionManager.getUserPhone()
        val email = sessionManager.getUserEmail()

        if (phone.isNullOrEmpty() || email.isNullOrEmpty()) {
            navController.navigate("updateProfile") {
                popUpTo("home") { inclusive = true }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
//            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) // Only top corners curved
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) // Only top corners curved
                .padding(0.dp)
                .background(
                    Color.Black
                )
        ) {
            GoogleMapView(userLocation, source, destination, polylinePoints,isRider=isRider )
            Spacer(modifier = Modifier.height(10.dp)
                .clip(RoundedCornerShape(25.dp)))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFFE0E0E0)) // or use a Material color
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehicleIcon(iconRes = R.drawable.bike1, label = "Bike")
                    VehicleIcon(iconRes = R.drawable.bike1, label = "Car")
                    VehicleIcon(iconRes = R.drawable.bike1, label = "SUV")
                    VehicleIcon(iconRes = R.drawable.bike1, label = "Taxi")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))


            GooglePlacesDropdown(
                label = "Source",
                placePredictions = placePredictions,
                onPlaceSelected = { name, lat, lng ->
                    selectedSource = name
                    sourceLat = lat
                    sourceLng = lng
                    source = LatLng(lat, lng)
                    if (destination != null) {
                        fetchPolyline(LatLng(lat, lng), destination!!) { points ->
                            polylinePoints = points
                        }
                    }
                },
                onSearch = { fetchPlaces(it) },
                currentLocation = "Your current location",
                currentLat = 0.0,
                currentLng = 0.0
            )

//            Spacer(modifier = Modifier.height(10.dp))

            GooglePlacesDropdown(
                label = "Destination",
                placePredictions = placePredictions,
                onPlaceSelected = { name, lat, lng ->
                    selectedDestination = name
                    destinationLat = lat
                    destinationLng = lng
                    destination = LatLng(lat, lng)
                    if (source != null) {
                        fetchPolyline(source!!, LatLng(lat, lng)) { points ->
                            polylinePoints = points
                        }
                    }
                },
                onSearch = { fetchPlaces(it) },
                currentLocation = "Your current location",
                currentLat = 0.0,
                currentLng = 0.0
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (isRider) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = route,
                    onValueChange = { route = it },
                    label = { Text("Route") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.inverseSurface),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = availableSeats,
                    onValueChange = { availableSeats = it },
                    label = { Text("Available Seats") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.inverseSurface),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                TimePicker(label = "Ride begins at") {
                    rideStartTime = it
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pillion", color = MaterialTheme.colorScheme.inverseSurface)
                Switch(
                    modifier = Modifier.padding(horizontal = 8.dp).height(36.dp),
                    checked = isRider,
                    onCheckedChange = { isRider = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.6f)
                    )
                )
                Text("Rider", color = MaterialTheme.colorScheme.inverseSurface)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Button(
            modifier = Modifier.wrapContentSize(),
            onClick = {
                val role = if (isRider) "Rider" else "Pillion"
                if (isRider) {
                    submitRide()
                } else {
                    if (selectedSource.isBlank() || selectedDestination.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show()
                    } else {
                        navController.navigate("availableRides/$selectedSource/$sourceLat/$sourceLng/$selectedDestination/$destinationLat/$destinationLng")
                    }
                }
            }
        ) {
            Text(
                text = if (isRider) "Add your ride" else "Search for your ride buddy",
                color = MaterialTheme.colorScheme.inverseSurface
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 30.dp, 16.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .weight(0.7f)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFF161e33))
                .clickable { navController.navigate("searchScreen") }
                .padding(14.dp)
        ) {
            Text(
                text = "Find your Ride buddy..",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.W500
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(0.15f)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF161e33))
                .clickable { navController.navigate("profile") }
                .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(0.15f)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF161e33))
                .clickable { navController.navigate("rideRequests") }
                .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}





//@Composable
//fun GoogleMapView(
//    userLocation: LatLng?,
//    source: LatLng?,
//    destination: LatLng?,
//    polylinePoints: List<LatLng>,
//    isRider: Boolean // ⬅️ Add this parameter
//) {
//    val cameraPositionState = rememberCameraPositionState()
//
//    // Move camera to source or destination if set
//    LaunchedEffect(source, destination) {
//        when {
//            destination != null -> {
//                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(destination, 15f))
//            }
//            source != null -> {
//                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(source, 15f))
//            }
//            userLocation != null -> {
//                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
//            }
//        }
//    }
//
//    // Auto-zoom to fit polyline
//    LaunchedEffect(polylinePoints) {
//        if (polylinePoints.isNotEmpty()) {
//            val boundsBuilder = LatLngBounds.builder()
//            polylinePoints.forEach { boundsBuilder.include(it) }
//            val bounds = boundsBuilder.build()
//            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
//        }
//    }
//    val mapHeight by animateDpAsState(
//        targetValue = if (isRider) 320.dp else 500.dp,
//        animationSpec = tween(durationMillis = 200) // optional: customize animation
//    )
//
//    GoogleMap(
//        modifier = Modifier.height(mapHeight),
//        cameraPositionState = cameraPositionState,
//        properties = MapProperties(
//            isMyLocationEnabled = true,
//            mapType = MapType.NORMAL,
//            isTrafficEnabled = true
//        ),
//        uiSettings = MapUiSettings(
//            zoomControlsEnabled = false,
//            compassEnabled = false,
//            myLocationButtonEnabled = false,
//            tiltGesturesEnabled = true,
//            scrollGesturesEnabled = true
//        )
//    ) {
//        source?.let {
//            Marker(
//                state = MarkerState(position = it),
//                title = "Source"
//            )
//
//        }
//
//        destination?.let {
//            Marker(
//                state = MarkerState(position = it),
//                title = "Destination"
//            )
//
//        }
//
//        if (polylinePoints.isNotEmpty()) {
//            Polyline(
//                points = polylinePoints,
//                color = Color.Blue,
//                width = 13f,
//                startCap = RoundCap(),
//                endCap = RoundCap(),
//                jointType = JointType.ROUND
//            )
//        }
//    }
//}









suspend fun fetchLatLngFromPlaceName(context: Context, placeName: String): LatLng? {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(placeName, 1)
            val location = addresses?.firstOrNull()
            location?.let {
                LatLng(it.latitude, it.longitude)
            }
        } catch (e: Exception) {
            null
        }
    }
}


suspend fun fetchRoute(source: LatLng, destination: LatLng): List<LatLng> {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.MAPS_API_KEY
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${source.latitude},${source.longitude}" +
                    "&destination=${destination.latitude},${destination.longitude}" +
                    "&key=$apiKey"

            val response = URL(url).readText()
            parseRoute(response)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

fun parseRoute(response: String): List<LatLng> {
    val json = JSONObject(response)
    val routes = json.getJSONArray("routes")
    if (routes.length() == 0) return emptyList()

    val overviewPolyline = routes.getJSONObject(0)
        .getJSONObject("overview_polyline")
        .getString("points")

    return decodePolyline(overviewPolyline)
}

fun decodePolyline(encoded: String): List<LatLng> {
    val polyline = mutableListOf<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var shift = 0
        var result = 0
        do {
            val b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        lat += if (result and 1 != 0) (result shr 1).inv() else result shr 1

        shift = 0
        result = 0
        do {
            val b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        lng += if (result and 1 != 0) (result shr 1).inv() else result shr 1

        polyline.add(LatLng(lat / 1E5, lng / 1E5))
    }
    return polyline
}

// Fetch user's current location and convert to an address
@SuppressLint("MissingPermission")
suspend fun fetchUserLocation(context: Context, fusedLocationClient: FusedLocationProviderClient): Pair<LatLng?, String?> {
    return withContext(Dispatchers.IO) {
        try {
            val locationTask = fusedLocationClient.lastLocation
            val location: Location? = Tasks.await(locationTask)
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                val address = getAddressFromLatLng(context, latLng)
                Pair(latLng, address)
            } ?: Pair(null, null)
        } catch (e: Exception) {
            Pair(null, null)
        }
    }
}

// Convert LatLng to a readable address using Geocoder
fun getAddressFromLatLng(context: Context, latLng: LatLng): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        addresses?.firstOrNull()?.getAddressLine(0)
    } catch (e: Exception) {
        null
    }
}


fun fetchPolyline(
    origin: LatLng,
    destination: LatLng,
    onResult: (List<LatLng>) -> Unit
) {
    val apiKey = "YOUR_GOOGLE_MAPS_API_KEY"
    val url = "https://maps.googleapis.com/maps/api/directions/json?" +
            "origin=${origin.latitude},${origin.longitude}" +
            "&destination=${destination.latitude},${destination.longitude}" +
            "&key=$apiKey"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = URL(url).readText()
            val json = JSONObject(response)
            val routes = json.getJSONArray("routes")

            if (routes.length() > 0) {
                val points = mutableListOf<LatLng>()
                val steps = routes.getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)
                    .getJSONArray("steps")

                for (i in 0 until steps.length()) {
                    val step = steps.getJSONObject(i)
                    val polyline = step.getJSONObject("polyline").getString("points")
                    points += decodePolyline(polyline)
                }

                withContext(Dispatchers.Main) {
                    onResult(points)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}




fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController)
}


@Composable
fun VehicleIcon(@DrawableRes iconRes: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(6.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
