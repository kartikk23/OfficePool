package com.agile.officepool.screens

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.*


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import com.agile.officepool.BuildConfig

import com.agile.officepool.components.GooglePlacesDropdown
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline

import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Tasks
import com.google.maps.android.compose.MapProperties


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var name by remember { mutableStateOf(TextFieldValue("")) }

    var source by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var selectedSource by remember { mutableStateOf<String?>(null) }
    var selectedDestination by remember { mutableStateOf<String?>(null) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    var location by remember { mutableStateOf<String?>(null) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

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
    }


    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Set the background color
                    titleContentColor = MaterialTheme.colorScheme.onBackground, // Set the title text color
                    navigationIconContentColor = Color.White // Set the navigation icon color
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp), // Add padding for better spacing
                        verticalAlignment = Alignment.CenterVertically, // Align children vertically in the center
                        horizontalArrangement = Arrangement.SpaceBetween, // Space out children horizontally

                    )

                    {


                        Column(
                            modifier = Modifier.weight(1f) // Take up remaining space in the Row
                        )
                        {
                            BasicTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (name.text.isEmpty()) {
                                        Text("Tejas Katke",
                                            style = TextStyle(
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.W600

                                            ), )
                                    }
                                    innerTextField()
                                },
                                enabled = false // Disable editing
                            )
                            location?.let {
                                BasicTextField(
                                    value = it,
                                    onValueChange = { location = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (it.isEmpty()) {
                                            Text(location ?: "Fetching location...", style = TextStyle(

                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.W400,
                                                color = Color.Gray



                                            ), )
                                        }
                                        innerTextField()
                                    },
                                    maxLines = 1,
                                    enabled = false // Disable editing
                                )
                            }
                        }
                        IconButton(onClick = {navController.navigate("profile") }) {
                            Icon(Icons.Default.Person, contentDescription = "Profile",Modifier.size(34.dp))
                        }
                    }
                },
//                navigationIcon = {
//
//                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("newScreen") },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Navigate")
            }
        },
        modifier = Modifier
            .fillMaxSize()



    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Placeholder for Map (Replace with your map implementation)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ){
                GoogleMapView(userLocation, source, destination, polylinePoints)

            }

            Box(
                modifier = Modifier
                    .wrapContentHeight()

                    .background(MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(0.dp,0.dp,12.dp,12.dp)
                    ),


            ){
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(0.dp,10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {


                    val context = LocalContext.current

// Google Places Dropdown for Source
                    GooglePlacesDropdown("Source") { placeName ->
                        selectedSource = placeName  // Store selected place name
                    }

// Google Places Dropdown for Destination
                    GooglePlacesDropdown("Destination") { placeName ->
                        selectedDestination = placeName // Store selected place name
                    }

// Convert source place name to LatLng
                    LaunchedEffect(selectedSource) {
                        selectedSource?.let {
                            source = fetchLatLngFromPlaceName(context, it) // Convert to LatLng
                        }
                    }

// Convert destination place name to LatLng
                    LaunchedEffect(selectedDestination) {
                        selectedDestination?.let {
                            destination = fetchLatLngFromPlaceName(context, it) // Convert to LatLng
                        }
                    }

// Fetch route when both source and destination are set
                    LaunchedEffect(source, destination) {
                        if (source != null && destination != null) {
                            polylinePoints = fetchRoute(source!!, destination!!)
                        }
                    }


                    // OutlinedTextField for Destination
//                    OutlinedTextField(
//                        value = destination,
//                        onValueChange = { destination = it },
//                        modifier = Modifier.fillMaxWidth(),
//                        label = { Text("Enter Destination") }, // Placeholder label
//                        placeholder = {
//                            Text(
//                                "Enter Destination",
//                                color = Color.Gray
//                            )
//                        }, // Gray placeholder text
//                        singleLine = true // Restrict to single line
//                    )
                }
            }

        }
    }
}

@Composable
fun NewScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text("New Screen", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun GoogleMapView(userLocation: LatLng?, source: LatLng?, destination: LatLng?, polylinePoints: List<LatLng>) {
    val cameraPositionState = rememberCameraPositionState()

    val sourceMarkerState = rememberMarkerState()
    val destinationMarkerState = rememberMarkerState()

    // Move camera to user location initially
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
        }
    }

    // Update source marker dynamically
    LaunchedEffect(source) {
        source?.let {
            sourceMarkerState.position = it
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
        }
    }

    // Update destination marker dynamically
    LaunchedEffect(destination) {
        destination?.let {
            destinationMarkerState.position = it
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 17f))
        }
    }

    // Auto-zoom to fit polyline when route updates
    LaunchedEffect(polylinePoints) {
        if (polylinePoints.isNotEmpty()) {
            val bounds = LatLngBounds.builder().apply {
                polylinePoints.forEach { include(it) }
            }.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true) // ✅ Enable blue dot
    ) {
        // Focused marker for user's location
//        userLocation?.let {
//            Marker(
//                state = rememberMarkerState(position = it),
//                title = "You are here",
//
//            )
//        }

        // Source Marker
        if (source != null) {
            Marker(state = sourceMarkerState, title = "Source")

        }

        // Destination Marker
        if (destination != null) {
            Marker(state = destinationMarkerState, title = "Destination")
        }

        // Draw polyline route dynamically
        if (polylinePoints.isNotEmpty()) {
            Polyline(points = polylinePoints, color = Color.Blue, width = 8f)
        }
    }
}








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

