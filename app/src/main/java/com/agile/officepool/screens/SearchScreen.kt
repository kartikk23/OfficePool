package com.agile.officepool.screens

import GooglePlacesDropdown
import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.R
import com.agile.officepool.components.TimePicker
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import com.agile.officepool.network.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
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
import getRoutePolylineWithInfo
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

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
    var rideStartTime by remember { mutableStateOf<LocalTime?>(null) }
    var rideDate by remember { mutableStateOf<LocalDate?>(null) }
    val statusOptions =
        listOf("Yet To Start", "Active", "Completed", "Cancelled",) // ✅ Dropdown options
    var status by remember { mutableStateOf(statusOptions[0]) } // Default selection
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    var availableSeats by remember { mutableStateOf("") }
    val sessionManager = SessionManager(LocalContext.current)
    val userId = sessionManager.getUserId() ?: ""
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var polylinePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

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
                rideStartTime.toString().isBlank() || rideDate.toString()
                    .isBlank() || route.isBlank() || status.isBlank() || availableSeats.isBlank()
            ) {
                isLoading = false
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
                rideStartTime = rideStartTime!!.toString(),
                rideDate = rideDate!!.toString()
            )

            try {
                val response = RetrofitClient.instance.addRide(rideInfo)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Ride added successfully!", Toast.LENGTH_LONG).show()
                    isLoading = false
                    navController.navigate("startUp") // Navigate after success
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AddRideError", "Failed to add ride: $errorBody")
                    Toast.makeText(context, "Failed to add ride", Toast.LENGTH_LONG).show()
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("AddRideException", "Exception occurred while adding ride", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

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
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_LONG).show()
        }
    }


// When source/destination change, launch coroutine to get polyline
    LaunchedEffect(sourceLat, sourceLng, destinationLat, destinationLng) {
        if (sourceLat != null && sourceLng != null && destinationLat != null && destinationLng != null) {
            source = LatLng(sourceLat!!, sourceLng!!)
            destination = LatLng(destinationLat!!, destinationLng!!)
            try {

                val routeResult = getRoutePolylineWithInfo(
                    source = source!!,
                    destination = destination!!,
                    apiKey = BuildConfig.MAPS_API_KEY
                )
                polylinePoints = routeResult.polyline

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 15.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            IconButton(
                modifier = Modifier.padding(start = 10.dp).weight(1.5f),
                onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.inverseSurface,
                )
            }

            // Floating Header Text
            Text(
                text = "Find your Ride Buddy",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(vertical = 20.dp).weight(8.5f),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // ✅ Make screen scrollable,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Column for icons and dotted line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 8.dp)
                        .height(100.dp) // Adjust as needed
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "From",
                        tint = Color.Gray
                    )

                    Canvas(modifier = Modifier.weight(1f)) {
                        val canvasHeight = size.height
                        val dashHeight = 10f
                        val gap = 10f
                        var y = 0f
                        while (y < canvasHeight) {
                            drawLine(
                                color = Color.Gray,
                                start = Offset(x = size.width / 2, y = y),
                                end = Offset(x = size.width / 2, y = y + dashHeight),
                                strokeWidth = 2f
                            )
                            y += dashHeight + gap
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = "To",
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        currentLat = userLocation?.latitude,
                        currentLng = userLocation?.longitude
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
                        currentLat = userLocation?.latitude,
                        currentLng = userLocation?.longitude
                    )
                }
            }

            // Rider / Pillion Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pillion",
                    fontSize = 15.sp,
                    color = Color.Gray
                )
                Switch(
                    checked = isRider,
                    onCheckedChange = { isRider = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.padding(horizontal = 5.dp).scale(0.8f)
                )
                Text(
                    text = "Rider",
                    fontSize = 15.sp,
                    color = Color.Gray
                )
            }

            if (isRider) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        value = route,
                        onValueChange = { route = it },
                        label = { Text("Route", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.inverseSurface
                        ),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedLabelColor = Color.Gray,

                        ),
                    )

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        value = availableSeats,
                        onValueChange = { availableSeats = it },
                        label = { Text("Available Seats", fontSize = 14.sp) },
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between date and time pickers
                    ) {
                        // Date Picker
                        Box(modifier = Modifier.weight(1f)) {
                            ShowDatePicker(
                                context = LocalContext.current,
                                selectedDate = rideDate
                            ) { pickedDate ->
                                rideDate = pickedDate
                                println("Selected Date: $pickedDate")
                            }
                        }

                        // Time Picker
                        Box(modifier = Modifier.weight(1f)) {
                            CustomTimePicker(
                                label = "Ride begins at",
                                selectedTime = rideStartTime
                            ) {
                                rideStartTime = it
                                println("Selected Time: $it")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), // ✅ Adjust height as you like
                shape = RoundedCornerShape(16.dp)
            ) {
                // Map Background
                GoogleMapView(userLocation, source, destination, polylinePoints, isRider = isRider)

            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    onClick = {
                        isLoading = true
                        val role = if (isRider) "Rider" else "Pillion"
                        println("Selected Role: $role")
                        if (isRider) {
                            submitRide()
                        } else {
                            if (selectedSource.isNullOrBlank() || selectedDestination.isNullOrBlank()) {
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Please fill in all fields",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                isLoading = false
                                navController.navigate("availableRides/${selectedSource}/${sourceLat}/${sourceLng}/${selectedDestination}/${destinationLat}/${destinationLng}")
                            }
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isRider) Icons.Default.AddCircle else Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.inverseSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isRider) "Post ride" else "Find your ride",
                                color = MaterialTheme.colorScheme.inverseSurface
                            )
                        }
                    }


                }
            }
        }
    }
}





@Composable
fun ShowDatePicker(context: Context, selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(pickedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // You can use a clickable component to show the DatePicker
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { datePickerDialog.show() }
    ) {
        Text(
            text = "Select Date",  // Or your label here
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.weight(8.5f),
                text = selectedDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))?: "", // Display the formatted date or a default string
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                modifier = Modifier.weight(1.5f),
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CustomTimePicker(
    label: String,
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                val selected = LocalTime.of(hour, minute)
                onTimeSelected(selected)
            },
            selectedTime?.hour ?: 12,
            selectedTime?.minute ?: 0,
            false
        )
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { timePickerDialog.show() }
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth()
        ) {

            Text(
                modifier = Modifier.weight(8.5f),
                text = selectedTime?.format(formatter) ?: "",
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                modifier = Modifier.weight(1.5f),
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
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
    val context = LocalContext.current
    val mapStyleOptions = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
    }
    // Move camera to source or destination if set
    LaunchedEffect(source, destination, userLocation) {
        if (source == null && destination == null && userLocation != null) {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
        } else if (source != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(source, 15f))
        } else if (destination != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(destination, 15f))
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
            isTrafficEnabled = false,
            mapStyleOptions = mapStyleOptions
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            zoomGesturesEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false,
            tiltGesturesEnabled = false,
            scrollGesturesEnabled = false
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
                color = Color.Black,
                width = 10f,
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