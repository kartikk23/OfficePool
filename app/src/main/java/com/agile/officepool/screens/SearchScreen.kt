package com.agile.officepool.screens

import GooglePlacesDropdown
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.components.TimePicker
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var source by remember { mutableStateOf<LatLng?>(null) }
    var destination by remember { mutableStateOf<LatLng?>(null) }
    var selectedSource by remember { mutableStateOf<String?>(null) }
    var sourceLat by remember { mutableStateOf<Double?>(null) }
    var sourceLng by remember { mutableStateOf<Double?>(null) }
    var selectedDestination by remember { mutableStateOf<String?>(null) }
    var destinationLat by remember { mutableStateOf<Double?>(null) }
    var destinationLng by remember { mutableStateOf<Double?>(null) }
    var isRider by remember { mutableStateOf(false) }
    var route by remember { mutableStateOf("") }
    var rideStartTime by remember { mutableStateOf("") }
    val statusOptions =
        listOf("Yet To Start", "Active", "Completed", "Cancelled",) // ✅ Dropdown options
    var status by remember { mutableStateOf(statusOptions[0]) } // Default selection
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    var availableSeats by remember { mutableStateOf("") }
    val sessionManager = SessionManager(LocalContext.current)
    val userId = sessionManager.getUserId() ?: ""
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

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

    // Function to handle ride submission
    fun submitRide() {
        coroutineScope.launch {
            if (selectedSource.isNullOrBlank() || selectedDestination.isNullOrBlank() ||
                rideStartTime.isBlank() || route.isBlank() || status.isBlank() || availableSeats.isBlank()
            ) {
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState()) // ✅ Make screen scrollable
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // ✅ Adjust height as you like
        ) {
            // Map Background
            GoogleMapView(userLocation, source, destination, polylinePoints,isRider=isRider )

            // Floating Header Text
            Text(
                text = "Find your Ride Buddy",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier
                    .align(Alignment.TopCenter) // ✅ Centered Top
                    .padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            GooglePlacesDropdown(
                label = "Source",
                placePredictions = placePredictions,
                onPlaceSelected = { name, lat, lng ->
                    selectedSource = name
                    sourceLat = lat
                    sourceLng = lng
                },
                onSearch = { fetchPlaces(it) },
                currentLocation = "Your current location",
                currentLat = 0.0,
                currentLng = 0.0
            )

//        Spacer(modifier = Modifier.height(10.dp))

            GooglePlacesDropdown(
                label = "Destination",
                placePredictions = placePredictions,
                onPlaceSelected = { name, lat, lng ->
                    selectedDestination = name
                    destinationLat = lat
                    destinationLng = lng
                },
                onSearch = { fetchPlaces(it) },
                currentLocation = "Your current location",
                currentLat = 0.0,
                currentLng = 0.0
            )

            // Rider / Pillion Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Pillion", color = MaterialTheme.colorScheme.inverseSurface)
                Switch(
                    checked = isRider,
                    onCheckedChange = { isRider = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(text = "Rider", color = MaterialTheme.colorScheme.inverseSurface)
            }

            if (isRider) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = route,
                        onValueChange = { route = it },
                        label = { Text("Route") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.inverseSurface
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedLabelColor = Color.Gray
                        ),
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = availableSeats,
                        onValueChange = { availableSeats = it },
                        label = { Text("Available Seats") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.inverseSurface
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedLabelColor = Color.Gray
                        ),
                    )

                    TimePicker(label = "Ride begins at") { selectedTime ->
                        rideStartTime = selectedTime
                        println("Selected Time: $selectedTime")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val role = if (isRider) "Rider" else "Pillion"
                    println("Selected Role: $role")
                    if (isRider) {
                        submitRide()
                    } else {
                        if (selectedSource.isNullOrBlank() || selectedDestination.isNullOrBlank()) {
                            Toast.makeText(
                                context,
                                "Please fill in all fields",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            navController.navigate("availableRides/${selectedSource}/${sourceLat}/${sourceLng}/${selectedDestination}/${destinationLat}/${destinationLng}")
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
    }
}

@Composable
fun GoogleMapView(
    userLocation: LatLng?,
    source: LatLng?,
    destination: LatLng?,
    polylinePoints: List<LatLng>,
    isRider: Boolean // ⬅️ Add this parameter
) {
    val cameraPositionState = rememberCameraPositionState()

    // Move camera to source or destination if set
    LaunchedEffect(source, destination) {
        when {
            destination != null -> {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(destination, 15f))
            }
            source != null -> {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(source, 15f))
            }
            userLocation != null -> {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
            }
        }
    }

    // Auto-zoom to fit polyline
    LaunchedEffect(polylinePoints) {
        if (polylinePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            polylinePoints.forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }
    val mapHeight by animateDpAsState(
        targetValue = if (isRider) 320.dp else 500.dp,
        animationSpec = tween(durationMillis = 200) // optional: customize animation
    )

    GoogleMap(
        modifier = Modifier.height(mapHeight),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL,
            isTrafficEnabled = true
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false,
            tiltGesturesEnabled = true,
            scrollGesturesEnabled = true
        )
    ) {
        source?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Source"
            )

        }

        destination?.let {
            Marker(
                state = MarkerState(position = it),
                title = "Destination"
            )

        }

        if (polylinePoints.isNotEmpty()) {
            Polyline(
                points = polylinePoints,
                color = Color.Blue,
                width = 13f,
                startCap = RoundCap(),
                endCap = RoundCap(),
                jointType = JointType.ROUND
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val navController = rememberNavController()
    SearchScreen(navController)
}