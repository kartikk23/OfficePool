package com.agile.officepool.screens

import GooglePlacesDropdown
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agile.officepool.BuildConfig
import com.agile.officepool.components.TimePicker
import com.agile.officepool.model.RideInfo
import com.agile.officepool.network.RetrofitClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var userId by remember { mutableStateOf("111") }
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
    val statusOptions = listOf("Yet To Start", "Active", "Completed","Cancelled", ) // ✅ Dropdown options
    var status by remember { mutableStateOf(statusOptions[0]) } // Default selection
    var expanded by remember { mutableStateOf(false) } // Dropdown state
    var availableSeats by remember { mutableStateOf("") }

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


    Column(
        modifier = Modifier.fillMaxSize().background(
            color = MaterialTheme.colorScheme.surface
        ).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes content apart
    ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(text = "Find your Ride buddy", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.inverseSurface)
        Spacer(modifier = Modifier.height(22.dp))

//         Google Places Dropdown for Source
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
        Spacer(modifier = Modifier.height(10.dp))



        if (isRider) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.inverseSurface
                ),
                value = route,
                onValueChange = { route = it },
                label = { Text("Route") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = Color.Gray
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.inverseSurface
                ),
                value = availableSeats,
                onValueChange = { availableSeats = it },
                label = { Text("Available Seats") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedLabelColor = Color.Gray
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))

//            ExposedDropdownMenuBox(
//                expanded = expanded,
//                onExpandedChange = { expanded = it }
//            ) {
//                OutlinedTextField(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .menuAnchor(), // Required for correct positioning
//                    textStyle = TextStyle(
//                        fontSize = 15.sp,
//                        color = MaterialTheme.colorScheme.inverseSurface
//                    ),
//                    value = status,
//                    onValueChange = {},
//                    label = { Text("Status") },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
//                    readOnly = true, // Prevent manual input
//                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) } ,// Drop-down arrow
//                    colors = TextFieldDefaults.colors(
//                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
//                        focusedContainerColor = MaterialTheme.colorScheme.surface,
//                        unfocusedLabelColor = Color.Gray
//                    ),
//
//                )
//                // Dropdown menu
//                ExposedDropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false }
//                ) {
//                    statusOptions.forEach { option ->
//                        DropdownMenuItem(
//                            text = { Text(option) },
//                            onClick = {
//                                status = option // Set selected value
//                                expanded = false // Close dropdown
//                            }
//                        )
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(10.dp))

            TimePicker(label = "Ride begins at") { selectedTime ->
                rideStartTime = selectedTime
                println("Selected Time: $selectedTime")
            }
            Spacer(modifier = Modifier.height(10.dp))


        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Pillion",color = MaterialTheme.colorScheme.inverseSurface)
            Switch(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .height(36.dp), // Adjusting height for better UI
                checked = isRider,
                onCheckedChange = { isRider = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.6f),
                ),
//                thumbContent = {
//                    if (isRider) {
//                        Text(text = "Rider", modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.inverseSurface)
//                    } else {
//                        Text(text = "Pillion", modifier = Modifier.fillMaxWidth(),color = MaterialTheme.colorScheme.inverseSurface)
//                    }
//                },

            )
            Text(text = "Rider",color = MaterialTheme.colorScheme.inverseSurface)
        }
        Spacer(modifier = Modifier.height(10.dp))
    }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
            val role = if (isRider) "Rider" else "Pillion"
            println("Selected Role: $role")
            if(isRider){
                submitRide()
            }else{
                if (selectedSource.isNullOrBlank() || selectedDestination.isNullOrBlank()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG).show()
                }else{
                    navController.navigate( "availableRides/${selectedSource}/${sourceLat}/${sourceLng}/${selectedDestination}/${destinationLat}/${destinationLng}")
                }
            }
        }) {
            Text(text = if (isRider) "Add your ride" else "Search for your ride buddy", color = MaterialTheme.colorScheme.inverseSurface)
        }
    }


}