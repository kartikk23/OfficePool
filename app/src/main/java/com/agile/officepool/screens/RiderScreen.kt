package com.agile.officepool.screens

import GooglePlacesDropdown
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.components.TransparentStatusBar

import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderScreen(navController: NavController) {
    BackHandler {
        // Custom behavior before navigating back
        navController.popBackStack()
    }
    var riderId by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf<String?>(null) }
    var sourceLat by remember { mutableStateOf<Double?>(null) }
    var sourceLng by remember { mutableStateOf<Double?>(null) }
    var selectedDestination by remember { mutableStateOf<String?>(null) }
    var destinationLat by remember { mutableStateOf<Double?>(null) }
    var destinationLng by remember { mutableStateOf<Double?>(null) }
    var route by remember { mutableStateOf("") }
    val statusOptions = listOf("Active", "Completed", "Cancelled", "Yet To Start") // ✅ Dropdown options
    var status by remember { mutableStateOf(statusOptions[0]) } // Default selection
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    var availableSeats by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Initialize the Places Client
    fun initializePlacesClient(context: Context): PlacesClient {
        if (!Places.isInitialized()) {
            Places.initialize(context, BuildConfig.MAPS_API_KEY)
        }
        return Places.createClient(context)
    }
    val placesClient = remember { initializePlacesClient(context) }

    TransparentStatusBar()


    // Function to handle ride submission
    fun submitRide() {
        coroutineScope.launch {
            if (selectedSource.isNullOrBlank() || selectedDestination.isNullOrBlank() ||
                riderId.isBlank() || route.isBlank() || status.isBlank() || availableSeats.isBlank()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show()
                return@launch
            }

            val rideInfo = RideInfo(
                riderId = riderId,
                source = selectedSource!!,
                destination = selectedDestination!!,
                sourceLat = sourceLat!!,
                sourceLng = sourceLng!!,
                destinationLat = destinationLat!!,
                destinationLng = destinationLng!!,
                route = route,
                status = status,
                availableSeats = availableSeats,
                rideStartTime = "Default"
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

    var placePredictions by remember { mutableStateOf(emptyList<AutocompletePrediction>()) }


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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add Ride", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.inverseOnSurface),
            value = riderId,
            onValueChange = { riderId = it },
            label = { Text("Rider ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) )
        Spacer(modifier = Modifier.height(10.dp))

        // Google Places Dropdown for Source
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

        Spacer(modifier = Modifier.height(10.dp))

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

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.sp,color = MaterialTheme.colorScheme.inverseOnSurface),
            value = route,
            onValueChange = { route = it },
            label = { Text("Route") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        Spacer(modifier = Modifier.height(10.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), // Required for correct positioning
                textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.inverseOnSurface),
                value = status,
                onValueChange = {},
                label = { Text("Status") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                readOnly = true, // Prevent manual input
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) } // Drop-down arrow
            )
            // Dropdown menu
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                statusOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            status = option // Set selected value
                            expanded = false // Close dropdown
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.inverseOnSurface),
            value = availableSeats,
            onValueChange = { availableSeats = it },
            label = { Text("Available Seats") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { submitRide() }) {
            Text("Submit Ride")
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text("Home screen")
        }
    }


}